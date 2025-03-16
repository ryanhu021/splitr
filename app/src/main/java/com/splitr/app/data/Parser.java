package com.splitr.app.data;

import java.util.*;

import com.google.mlkit.vision.text.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    public static ParserResult parseReceipt(Text text, int receiptId) {
        List<Item> items = new ArrayList<>();
        List<Text.Line> prices = new ArrayList<>();
        String priceRegex = "\\$\\d{1,3}(?:\\s*[.,]\\s*\\d{2})";
        Pattern pattern = Pattern.compile(priceRegex);

        // iterate through all blocks
        for (Text.TextBlock block : text.getTextBlocks()) {
            // iterate through all lines in the block
            for (Text.Line line : block.getLines()) {
                Matcher matcher = pattern.matcher(line.getText().trim());
                if (line.getText().startsWith("$") && matcher.find()) {
                    // replace commas with periods
                    prices.add(line);
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
            // add item to the list
            Item item = new Item(i, receiptId, closestLine.getText().trim(), Double.parseDouble(extractPrice(price).substring(1)), 1);
            items.add(item);
        }

        // create new ParserResult
        return new ParserResult("Trader Joe's", "2025-03-15", Double.parseDouble(lastPrice.substring(1)), items);
    }

    private static String extractPrice(Text.Line priceLine) {
        return priceLine.getText().trim().replace(",", ".").replaceAll("\\s+", "");
    }
}
