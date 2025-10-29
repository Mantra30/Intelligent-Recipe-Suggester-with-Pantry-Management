package com.smartrecipes.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements fuzzy string matching for ingredient suggestions
 */
public class FuzzyMatcher {
    
    /**
     * Calculate similarity between two strings using Levenshtein distance
     */
    public static double calculateSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) return 0.0;
        if (s1.equals(s2)) return 1.0;
        
        int maxLength = Math.max(s1.length(), s2.length());
        if (maxLength == 0) return 1.0;
        
        int distance = levenshteinDistance(s1.toLowerCase(), s2.toLowerCase());
        return 1.0 - (double) distance / maxLength;
    }
    
    /**
     * Calculate Levenshtein distance between two strings
     */
    private static int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        // Initialize base cases
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }
        
        // Fill the DP table
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = Math.min(Math.min(dp[i - 1][j], dp[i][j - 1]), dp[i - 1][j - 1]) + 1;
                }
            }
        }
        
        return dp[s1.length()][s2.length()];
    }
    
    /**
     * Find best matches from a list of strings
     */
    public static List<String> findBestMatches(String query, List<String> candidates, int maxResults) {
        if (query == null || query.trim().isEmpty() || candidates == null) {
            return new ArrayList<>();
        }
        
        List<MatchResult> results = new ArrayList<>();
        
        for (String candidate : candidates) {
            double similarity = calculateSimilarity(query, candidate);
            if (similarity > 0.3) { // Minimum threshold
                results.add(new MatchResult(candidate, similarity));
            }
        }
        
        // Sort by similarity (descending)
        results.sort((r1, r2) -> Double.compare(r2.similarity, r1.similarity));
        
        // Return top matches
        List<String> matches = new ArrayList<>();
        for (int i = 0; i < Math.min(maxResults, results.size()); i++) {
            matches.add(results.get(i).text);
        }
        
        return matches;
    }
    
    /**
     * Check if two strings are similar enough to be considered a match
     */
    public static boolean isSimilar(String s1, String s2, double threshold) {
        return calculateSimilarity(s1, s2) >= threshold;
    }
    
    /**
     * Extract keywords from a string for better matching
     */
    public static List<String> extractKeywords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        List<String> keywords = new ArrayList<>();
        String[] words = text.toLowerCase().split("\\s+");
        
        for (String word : words) {
            // Remove common words and short words
            if (word.length() > 2 && !isCommonWord(word)) {
                keywords.add(word);
            }
        }
        
        return keywords;
    }
    
    /**
     * Check if a word is a common word that should be ignored
     */
    private static boolean isCommonWord(String word) {
        String[] commonWords = {
            "the", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with", "by",
            "a", "an", "is", "are", "was", "were", "be", "been", "being", "have", "has",
            "had", "do", "does", "did", "will", "would", "could", "should", "may", "might",
            "can", "must", "shall", "this", "that", "these", "those", "i", "you", "he", "she",
            "it", "we", "they", "me", "him", "her", "us", "them", "my", "your", "his", "her",
            "its", "our", "their", "cup", "cups", "tsp", "tbsp", "oz", "lb", "kg", "g", "ml", "l"
        };
        
        for (String common : commonWords) {
            if (word.equals(common)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Calculate partial match score for ingredient suggestions
     */
    public static double calculatePartialMatch(String query, String candidate) {
        if (query == null || candidate == null) return 0.0;
        
        String normalizedQuery = query.toLowerCase().trim();
        String normalizedCandidate = candidate.toLowerCase().trim();
        
        // Direct substring match
        if (normalizedCandidate.contains(normalizedQuery)) {
            return 0.8;
        }
        if (normalizedQuery.contains(normalizedCandidate)) {
            return 0.7;
        }
        
        // Word-based matching
        String[] queryWords = normalizedQuery.split("\\s+");
        String[] candidateWords = normalizedCandidate.split("\\s+");
        
        int matches = 0;
        for (String queryWord : queryWords) {
            for (String candidateWord : candidateWords) {
                if (candidateWord.contains(queryWord) || queryWord.contains(candidateWord)) {
                    matches++;
                    break;
                }
            }
        }
        
        if (queryWords.length > 0) {
            return (double) matches / queryWords.length;
        }
        
        return 0.0;
    }
    
    /**
     * Helper class for storing match results
     */
    private static class MatchResult {
        final String text;
        final double similarity;
        
        MatchResult(String text, double similarity) {
            this.text = text;
            this.similarity = similarity;
        }
    }
}
