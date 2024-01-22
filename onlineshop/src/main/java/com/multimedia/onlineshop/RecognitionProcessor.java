package com.multimedia.onlineshop;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.multimedia.onlineshop.models.CommandData;
import com.multimedia.onlineshop.models.KeyValue;
import com.multimedia.onlineshop.models.ProductType;
import com.multimedia.onlineshop.repositories.ProductTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

@Service
public class RecognitionProcessor {

    private static final String mainContext =
        "A user gave me his request. Please determine the commands and reply with only the json array. " +
        "User may want to do one or many of the following commands: (search for something, cancel search, " +
        "sort something, cancel sorting, filter by something, cancel filtering, switch pages, open item, close item). " +
        "Canceling can be identified by these or similar words: (cancel, discard, clear, reset). " +
        "The types of commands are the following: "+
        "1 - search: keywords: (search, search for, find, look for, look up, lookup, searching, finding), " +
        "examples: (find dinosaur, search for star wars, discard search, cancel searching); " +
        "2 - sort: keywords: (sort by, order by, sort, sorting, order, ordering), " +
        "examples: (sort by name ascending, order by price descending, cancel sotring, discard sort); " +
        "3 - filter: keywords: (filter by, show only, show, filter, filtering), " +
        "examples: (filter by vehicles, show only kitchenware, show sport items, discard filter, clear filter); " +
        "4 - change page: keywords: (go to page, switch to page, go pages forward, swipe pages back), " +
        "examples: (go to next page, switch to previous page, go to page five, switch to first page, switch to last page, go fifteen pages forward); " +
        "5 - open item: keywords: (open item, show element, select product), " +
        "examples: (open fifth item, show first element, select third item); " +
        "6 - close item: examples: (go back, close item, back to list, exit). "+
        "Extract each command from the string and add it to json array like [{\"type\": <number>, \"command\": <string>}, {...}, ...]. " +
        "Return the array. Here is the string: ";

    private static final Map<Integer, CommandData> commandDataMap;

    static {
        commandDataMap = new HashMap<>();
        commandDataMap.put(1, new CommandData(
            "search",
            "A user gave me his search request. Please determine what the user wants to find. "+
            "If user wants to (clear, cancel, discard, reset) (search, lookup, find), return empty string in json. "+
            "If search object is missing like \"Please search for\" return \"error\" in json. " +
            "Otherwise return the phrase to find in json object {\"result\": <phrase>}. Here is the request: ",
            "Sorry, I couldn't quite get your search request. You might have omitted the object of search. Please try again.",
            "You requested several search requests, however I can search only one thing at a time. " +
            "Please try again with one search request."
        ));
        commandDataMap.put(2, new CommandData(
            "sort",
            "A user gave me his sort request. Please determine property and ordering of sorting and reply with json. "+
            "Allowed values for sort property: (name, price), allowed values for sort ordering: (asc, desc). "+
            "If user wants to (clear, cancel, discard, reset) (sort, order, sorting, ordering), return empty string in json. "+
            "If property or ordering are missing like \"sort by\" (no property and ordering) or \"sort by descending\" (no property) return \"error\" in json. " +
            "If property is invalid (not name and not price) return \"error\" in json. "+
            "Otherwise return the property and ordering in json object with space between them {\"result\": <property ordering>}. "+
            "Here is the request: ",
            "Ooops, it seems that you tried to sort by wrong property, with wrong ordering or without specifying either of them. You may sort by " +
            "name or price with ascending or descending ordering, for example a to z for name or high to low for price. Please try again.",
            "You requested several sort requests, however I can sort only one property at a time. " +
            "Please try again with one sort request."
        ));
        commandDataMap.put(3, new CommandData(
            "filter",
            "A user gave me his filter request. Please determine the list of ids of related objects to filter request and reply with json. "+
            "If user wants to (clear, cancel, discard, reset) (filter, filtering), return empty string in json. "+
            "If request contains no objects to filter like \"filter by <no object here>\" return \"error\" in json. " +
            "The list of id - object pairs: {}. If no related objects were found in the list return \"error\" in json. "+
            "Return the ids separated by commas in json object {\"result\": <ids>}. Here is the request: ",
            "Sorry, the product types you want to filter by do not exist. If I am mistaken, please use a " +
            "more specific category in the request.",
            "You requested several filter requests, however I can process only one filter request at a time. " +
            "Please try again with one filter request."
        ));
        commandDataMap.put(4, new CommandData(
            "page",
            "A user gave me a page choice request. Please determine the result by following rules and return it in json object {\"result\": <value>}. Rules: "+
            "Only positive integers are allowed in request. Zero, negative or floating numbers are not allowed. If invalid number is requested or no number is present return \"error\""+
            "If definite page is asked, set the number of page (\"go to page five\" > \"5\", \"switch to eights page\" > \"8\"); "+
            "If next/previous page or n pages forward/backward were asked, set value to + or - and number of pages (\"go to next page\" > \"+1\", \"go to previous page\" > \"-1\", \"go thirteen pages forward\" > \"+13\", \"go five pages back\" > \"-5\"); "+
            "If asked to go to last page set value to \"last\" (\"switch to last page\" > \"last\"). "+
            "Here is the request: ",
            "Sorry, but you are trying to open an invalid page. Please specify an existing page number and I will open that page.",
            "You requested several page opening requests, however I can process only one such request at a time. " +
            "Please try again with one page opening request."
        ));
        commandDataMap.put(5, new CommandData(
            "item",
            "A user gave me an item select request. Please determine the result by following rules and return it in json object {\"result\": <value>}. Rules: "+
            "Only positive integers are allowed in request. Zero, negative, floating numbers or values like (previous, next) are not allowed. If invalid number or value is requested or no value is present return \"error\""+
            "If asked to open last item set value to \"last\". " +
            "If valid then set value to the number specified (\"show second item\" > \"2\", \"open element five\" > \"5\"). "+
            "Here is the request: ",
            "Sorry, but you are trying to open an invalid item. Please specify an existing item number and I will open that item.",
            "You requested several item opening requests, however I can process only one such request at a time. " +
            "Please try again with one item opening request."
        ));
        commandDataMap.put(6, new CommandData(
            "exit",
            null,
            null,
            "You requested several item closing requests, however it seems strange and unnecessary. Perhaps you meant something else? " +
            "Please rethink your request and try again."
        ));
    }

    @Value("${openai.key1}")
    private String openAIKey1;

    @Value("${openai.key2}")
    private String openAIKey2;

    private int callCount = 0;

    private static final String apiUrl = "https://api.openai.com/v1/chat/completions";

    private static final String model = "gpt-3.5-turbo";

    @Autowired
    private ProductTypeRepository productTypeRepository;

    public List<KeyValue> processRequest(String request) {
        try {
            String main = callChatGPT(mainContext + request);
            List<JsonElement> elementList = JsonParser.parseString(main).getAsJsonArray().asList();
            Map<Integer, Integer> countMap = getCountMap();
            for (JsonElement el : elementList) {
                int type = el.getAsJsonObject().get("type").getAsInt();
                Integer count = countMap.get(type);
                count ++;   //null exception handled in catch block
                countMap.put(type, count);
                if (count > 1) {
                    return Collections.singletonList(new KeyValue(
                        "error",
                        commandDataMap.get(type).getOverflowErrorMessage())
                    );
                }
            }
            int group1 = countMap.get(1) + countMap.get(2) + countMap.get(3) > 0 ? 1 : 0;
            int sum = group1 + countMap.get(4) + countMap.get(5) + countMap.get(6);
            if (sum > 1) {
                return Collections.singletonList(new KeyValue("error", "Sorry, you tried to combine a few different " +
                    "command types in your request, for example sorting and switching pages at the same time. " +
                    "Please use only one type of commands per request."));
            }
            else if (sum == 0) {
                return Collections.singletonList(new KeyValue("error", "Sorry, I couldn't detect any valid command " +
                    "in your request. Please try again."));
            }
            else {
                List<KeyValue> result = new ArrayList<>();
                for (JsonElement el : elementList) {
                    JsonObject obj = el.getAsJsonObject();
                    int type = obj.get("type").getAsInt();
                    String command = obj.get("command").getAsString();
                    if (command == null || "".equals(command)) {
                        continue;
                    }
                    String context = commandDataMap.get(type).getOpenAiContext();
                    if (type == 3) {
                        context = context.replace("{}", getProductTypeListString());
                    }
                    String response = "";
                    if (context != null) {
                        String jsonStr = callChatGPT(context + command);
                        JsonObject json = JsonParser.parseString(jsonStr).getAsJsonObject();
                        String commandResult = json.get("result").getAsString();
                        if (commandResult != null) {
                            if ("error".equals(commandResult)) {
                                return Collections.singletonList(new KeyValue("error",
                                    commandDataMap.get(type).getProcessingErrorMessage()));
                            }
                            else {
                                response = commandResult;
                            }
                        }
                        else {
                            throw new RuntimeException("Command is null. Can't process it");
                        }
                    }
                    result.add(new KeyValue(commandDataMap.get(type).getCommandType(), response));
                }
                if (result.size() == 0) {
                    result.add(new KeyValue("error", "Sorry, I couldn't detect any valid command " +
                        "in your request. Please try again."));
                }
                return result;
            }
        }
        catch (Exception e) {
            return Collections.singletonList(new KeyValue("error", "Sorry, something went wrong and I couldn't " +
                "process your request. You might have spoken off-topic and didn't give any commands. Please try again."));
        }
    }

    private String callChatGPT(String prompt) {
        callCount ++;
        if (callCount == 100) {
            callCount = 0;
        }
        String apiKey = callCount % 2 == 0 ? openAIKey1 : openAIKey2;
        try {
            URL obj = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setRequestProperty("Content-Type", "application/json");

            // The request body
            JsonObject inArray = new JsonObject();
            inArray.addProperty("role", "user");
            inArray.addProperty("content", prompt);

            JsonArray messageArray = new JsonArray();
            messageArray.add(inArray);

            JsonObject jsonBody = new JsonObject();
            jsonBody.addProperty("model", model);
            jsonBody.add("messages", messageArray);

            connection.setDoOutput(true);
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(jsonBody.toString());
            writer.flush();
            writer.close();

            // Response from ChatGPT
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuffer response = new StringBuffer();
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            br.close();
            JsonObject responseJson = JsonParser.parseString(response.toString()).getAsJsonObject();
            JsonObject choice = responseJson.get("choices").getAsJsonArray().get(0).getAsJsonObject();
            String result = choice.get("message").getAsJsonObject().get("content").getAsString();
            return result;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getProductTypeListString() {
        String result = "";
        boolean isFirst = true;
        List<ProductType> list = productTypeRepository.getAllProductTypes();
        for (ProductType pt : list) {
            if (isFirst) {
                isFirst = false;
            }
            else {
                result += ", ";
            }
            result += pt.getId() + " - " + pt.getName();
        }
        return result;
    }

    private Map<Integer, Integer> getCountMap() {
        Map<Integer, Integer> res = new HashMap<>();
        res.put(1, 0);
        res.put(2, 0);
        res.put(3, 0);
        res.put(4, 0);
        res.put(5, 0);
        res.put(6, 0);
        return res;
    }
}
