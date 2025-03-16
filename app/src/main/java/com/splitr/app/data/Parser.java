package com.splitr.app.data;

import com.google.mlkit.vision.text.Text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    private static final Pattern PRICE_MATCHER = Pattern.compile("(?:^|[-–—])\\$?\\d{1,3}[.,]\\d{2}[-–—]?");
    private static final Pattern DATE_MATCHER = Pattern.compile("\\b(?:\\d{4}[-/]\\d{1,2}[-/]\\d{1,2}|\\d{1,2}[-/]\\d{1,2}[-/]\\d{2,4})\\b\n");

    public static ParserResult parseReceipt(Text text, int receiptId) {
        List<Item> items = new ArrayList<>();
        List<Text.Line> prices = new ArrayList<>();
        String title = "";
        String date = "";

        // extract title
        if (!text.getTextBlocks().isEmpty()) {
            title = text.getTextBlocks().get(0).getLines().get(0).getText();
        }

        // iterate through all blocks
        for (Text.TextBlock block : text.getTextBlocks()) {
            // iterate through all lines in the block
            for (Text.Line line : block.getLines()) {
                // try matching as price
                Matcher priceMatcher = PRICE_MATCHER.matcher(line.getText().replaceAll("\\s+", ""));
                if (priceMatcher.find()) {
                    prices.add(line);
                } else {
                    // if not price, try matching as date
                    Matcher dateMatcher = DATE_MATCHER.matcher(line.getText());
                    if (dateMatcher.find()) {
                        System.out.println("Found date: " + line.getText());
                        date = extractDate(line);
                    }
                }
            }
        }

        // sort from top to bottom of receipt
        prices.sort(Comparator.comparingDouble(a -> a.getBoundingBox().exactCenterY()));

        // get last price (total price) and remove
        String lastPrice = extractPrice(prices.get(prices.size() - 1));
        prices.remove(prices.size() - 1);
        for (int i = prices.size() - 1; i >= 1; i--) {
            // remove all prices matching total price
            String cleanedPrice = extractPrice(prices.get(i));
            if (cleanedPrice.equals(lastPrice)) {
                prices.remove(i);
            } else {
                break;
            }
        }

        // match prices to items
        for (int i = 0; i < prices.size(); i++) {
            Text.Line price = prices.get(i);
            Text.Line closestLine = null; // keep track of vertically closest item to price
            double closestDistance = Double.MAX_VALUE; // keep track of distance to price
            for (Text.TextBlock block : text.getTextBlocks()) {
                // find the closest line to the price
                for (Text.Line line : block.getLines()) {
                    double newDistance = Math.abs(line.getBoundingBox().exactCenterY() - price.getBoundingBox().exactCenterY());
                    if (line.getBoundingBox().centerX() < price.getBoundingBox().centerX() &&
                            (closestLine == null || newDistance < closestDistance)) {
                        closestLine = line;
                        closestDistance = newDistance;
                    }
                }
            }

            double priceValue = Double.parseDouble(extractPrice(price));
            if (priceValue < 0.0 && !items.isEmpty()) {
                // apply discount to previous item if price is negative and round to 2 decimal places
                double discountedValue = Math.round((items.get(items.size() - 1).getPrice() + priceValue) * 100.0) / 100.0;
                items.get(items.size() - 1).setPrice(discountedValue);
            } else {
                // otherwise add as new item
                items.add(new Item(items.size(), receiptId, closestLine.getText().trim(), Double.parseDouble(extractPrice(price)), 1));
            }
        }

        // create new ParserResult
        return new ParserResult(title, date, Double.parseDouble(lastPrice), items);
    }

    private static String extractPrice(Text.Line priceLine) {
        String price = priceLine.getText().replaceAll("\\s+", ""); // Covers trim + internal spaces

        Matcher matcher = PRICE_MATCHER.matcher(price);
        if (matcher.find()) {
            String matchedPrice = matcher.group().replace("$", "").replaceAll(",", ".").replaceAll("[-–—]", "");
            System.out.println("Converted " + priceLine.getText() + " to " + matchedPrice);
            return price.contains("-") || price.contains("–") || price.contains("—")
                    ? "-" + matchedPrice
                    : matchedPrice;
        }

        return ""; // Return empty if no valid price found
    }

    private static String extractDate(Text.Line dateLine) {
        Matcher matcher = DATE_MATCHER.matcher(dateLine.getText());
        if (matcher.find()) {
            return matcher.group();
        }

        return ""; // Return empty if no valid date found
    }
}
