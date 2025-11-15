package me.touchie771.voting.utils;

/**
 * Utility class for parsing command arguments, including quoted arguments.
 */
public class ArgumentParser {
    
    /**
     * Represents the result of parsing a quoted argument.
     */
    public record ParseResult(String value, int argsConsumed) {}
    
    /**
     * Parses a quoted argument from the given args array starting at the specified index.
     * If the argument at startIndex is not quoted, it returns that single argument.
     * If it is quoted, it combines all arguments until the closing quote is found.
     * 
     * @param args the array of command arguments
     * @param startIndex the index to start parsing from
     * @return a ParseResult containing the parsed value and number of arguments consumed
     */
    public static ParseResult parseQuotedArgument(String[] args, int startIndex) {
        if (startIndex >= args.length) return new ParseResult("", 0);
        
        StringBuilder result = new StringBuilder();
        boolean inQuotes = false;
        int argsConsumed = 0;

        for (int i = startIndex; i < args.length; i++) {
            String arg = args[i];
            
            if (arg.startsWith("\"") && !inQuotes) {
                inQuotes = true;
                result.append(arg.substring(1));
                argsConsumed++;
            } else if (arg.endsWith("\"") && inQuotes) {
                result.append(" ").append(arg, 0, arg.length() - 1);
                argsConsumed++;
                break;
            } else if (inQuotes) {
                result.append(" ").append(arg);
                argsConsumed++;
            } else {
                return new ParseResult(arg, 1);
            }
        }
        
        return new ParseResult(result.toString(), argsConsumed);
    }
}
