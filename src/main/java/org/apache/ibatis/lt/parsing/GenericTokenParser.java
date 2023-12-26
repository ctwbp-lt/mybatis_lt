package org.apache.ibatis.lt.parsing;

import java.nio.charset.StandardCharsets;

public class GenericTokenParser {
    private final String openToken;

    private final String closeToken;

    private final TokenHandler handler;

    public GenericTokenParser(String openToken, String closeToken, TokenHandler handler) {
        this.openToken = openToken;
        this.closeToken = closeToken;
        this.handler = handler;
    }

    public String parse(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        // search open token
        int start = text.indexOf(openToken);
        // If openToken is not included
        if (start == -1) {
            return text;
        }

        char[] src = text.toCharArray();
        int offset = 0;
        final StringBuffer builder = new StringBuffer();
        StringBuffer expression = null;
        do {
            if (start > 0 && src[start - 1] == '\\') {
                // this open token is escaped. remove the backslash and continue.
                builder.append(src, offset, start - offset - 1).append(openToken);
                offset = start + openToken.length();
            } else {
                // found open token. let's search close token.
                if (expression == null){
                    expression = new StringBuffer();
                }else {
                    expression.setLength(0);
                }
                // Copy the content between offset and start
                builder.append(src, offset, start - offset);
                offset = start + openToken.length();
                int end = text.indexOf(closeToken, offset);
                while (end > -1){
                    if (end <= offset || (src[end -1] != '\\')){
                        expression.append(src, offset, end - offset);
                        break;
                    }
                    // this close token is escaped. remove the backslash and continue.
                    expression.append(src, offset, end-offset -1).append(closeToken);
                    offset = end + closeToken.length();
                    end = text.indexOf(closeToken,offset);
                }

                // If closeToken is not found
                if (end == -1){
                    builder.append(src, start, src.length -start);
                    offset = src.length;
                }else {
                    builder.append(handler.handlerToken(expression.toString()));
                    offset = end + closeToken.length();
                }
            }
            // find next openToken
            start = text.indexOf(openToken, offset);
        } while (start > -1);
        // process the remaining str
        if (offset < src.length){
            builder.append(src, offset, src.length - offset);
        }
        return builder.toString();
    }
}
