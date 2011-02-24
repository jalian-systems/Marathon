/*******************************************************************************
 *  
 *  Copyright (C) 2010 Jalian Systems Private Ltd.
 *  Copyright (C) 2010 Contributors to Marathon OSS Project
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Library General Public License for more details.
 * 
 *  You should have received a copy of the GNU Library General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 *  Project website: http://www.marathontesting.com
 *  Help: Marathon help forum @ http://groups.google.com/group/marathon-testing
 * 
 *******************************************************************************/
package net.sourceforge.marathon.component;

import java.util.ArrayList;
import java.util.Properties;

public class PropertyHelper {

    public static String toString(Properties p, String[] propOrder) {
        StringBuffer sb = new StringBuffer();

        if (p.size() > 1)
            sb.append("{");
        char[] convertBuf = new char[1024];
        for (int i = 0; i < propOrder.length; i++) {
            sb.append(escape(p.getProperty(propOrder[i]), convertBuf));
            if (i < propOrder.length - 1)
                sb.append(", ");
        }
        if (p.size() > 1)
            sb.append("}");
        return sb.toString();
    }

    private static String escape(String s, char[] convertBuf) {
        if (convertBuf.length < s.length()) {
            int newLen = s.length() * 2;
            if (newLen < 0) {
                newLen = Integer.MAX_VALUE;
            }
            convertBuf = new char[newLen];
        }
        int convertLen = 0;

        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            switch (chars[i]) {
            case '{':
            case '}':
            case '\\':
            case ',':
            case ':':
                convertBuf[convertLen++] = '\\';
                break;
            }
            convertBuf[convertLen++] = chars[i];
        }
        return new String(convertBuf, 0, convertLen);
    }

    public static String toString(Properties[] pa, String[] propOrder) {
        StringBuffer sb = new StringBuffer();

        sb.append("[");
        for (int i = 0; i < pa.length; i++) {
            sb.append(toString(pa[i], propOrder));
            if (i < pa.length - 1)
                sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }

    private final static class TokenReader {
        final static int ID = 1;
        final static int COMMA = ',';
        final static int OPENBR = '{';
        final static int CLOSEBR = '}';
        final static int COLON = ':';

        private char[] b;
        String text;
        int index;
        private int len;

        public TokenReader(String s) {
            b = s.toCharArray();
            len = b.length;
            index = 0;
        }

        public TokenReader(String s, int off, int len) {
            b = s.toCharArray();
            index = off;
            this.len = len;
        }

        public boolean hasNext() {
            return index < len;
        }

        public int next() {
            skipSpaces();
            if (index >= len)
                throw new RuntimeException("Invalid property list format");
            switch (b[index]) {
            case ',':
            case '{':
            case '}':
            case ':':
                return b[index++];
            default:
                StringBuffer sb = new StringBuffer();
                while (index < len && b[index] != ',' && b[index] != '{' && b[index] != '}' && b[index] != ':') {
                    if (b[index] == '\\') {
                        index++;
                        if (index >= len)
                            throw new RuntimeException("Invalid property list format");
                    }
                    sb.append(b[index]);
                    index++;
                }
                text = sb.toString().trim();
                return ID;
            }
        }

        private void skipSpaces() {
            while (index < len && b[index] == ' ')
                index++;
        }

        public String getText() {
            return text;
        }
    }

    public static Properties fromString(String s, String[][] props) {
        TokenReader reader = new TokenReader(s);

        return readProperties(reader, props);
    }

    public static Properties readProperties(TokenReader reader, String[][] props) {
        if (!reader.hasNext())
            throw new RuntimeException("Invalid property list format");

        int token = reader.next();
        if (token == TokenReader.ID) {
            Properties p = new Properties();
            for (int i = 0; i < props.length; i++) {
                if (props[i].length == 1) {
                    p.setProperty(props[i][0], reader.getText());
                    return p;
                }
            }
            throw new RuntimeException("Invalid property list format");
        }
        if (token == TokenReader.OPENBR) {
            ArrayList<String[]> list = new ArrayList<String[]>();
            while (token != TokenReader.CLOSEBR) {
                String[] tokens = new String[2];
                token = reader.next();
                if (token != TokenReader.ID)
                    throw new RuntimeException("Invalid property list format");
                tokens[0] = reader.getText();
                token = reader.next();
                if (token == TokenReader.COLON) {
                    token = reader.next();
                    if (token != TokenReader.ID)
                        throw new RuntimeException("Invalid property list format");
                    tokens[1] = reader.getText();
                    token = reader.next();
                    if (token != TokenReader.COMMA && token != TokenReader.CLOSEBR)
                        throw new RuntimeException("Invalid property list format");
                } else if (token == TokenReader.COMMA || token == TokenReader.CLOSEBR) {
                    tokens[1] = tokens[0];
                    tokens[0] = null;
                }
                list.add(tokens);
            }
            String[] first = (String[]) list.get(0);
            if (first[0] == null) {
                Properties p = new Properties();
                for (int i = 0; i < props.length; i++) {
                    if (props[i].length == list.size()) {
                        String[] selectedProps = props[i];
                        for (int j = 0; j < selectedProps.length; j++) {
                            p.setProperty(selectedProps[j], ((String[]) list.get(j))[1]);
                        }
                        return p;
                    }
                }
                throw new RuntimeException("Invalid property list format");
            }
            Properties p = new Properties();
            for (int i = 0; i < list.size(); i++) {
                String[] prop = (String[]) list.get(i);
                p.setProperty(prop[0], prop[1]);
            }
            return p;
        }
        throw new RuntimeException("Invalid property list format");
    }

    public static Properties[] fromStringToArray(String s, String[][] props) {
        s = s.trim();
        if (!s.startsWith("[") || !s.endsWith("]"))
            throw new RuntimeException("Invalid property list format");
        if (s.length() == 2)
            return new Properties[0];

        ArrayList<Properties> plist = new ArrayList<Properties>();
        int token = TokenReader.COMMA;
        TokenReader reader = new TokenReader(s, 1, s.length() - 1);
        while (token == TokenReader.COMMA && reader.hasNext()) {
            Properties p = readProperties(reader, props);
            plist.add(p);
            if (reader.hasNext())
                token = reader.next();
        }
        return (Properties[]) plist.toArray(new Properties[plist.size()]);
    }
}
