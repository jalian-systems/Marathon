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
package net.sourceforge.marathon.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * Matches filenames to given pattern
 */
public class FilePatternMatcher {
    private ArrayList<Pattern> hiddenFiles = new ArrayList<Pattern>();

    /**
     * Construct a FilePatternMatcher. The matchPatterns is a space delimeted
     * string with regular expressions to match with.
     * 
     * @param matchPatterns
     */
    public FilePatternMatcher(String matchPatterns) {
        if (matchPatterns != null) {
            StringTokenizer toke = new StringTokenizer(matchPatterns);
            while (toke.hasMoreTokens()) {
                hiddenFiles.add(Pattern.compile(toke.nextToken()));
            }
        }
    }

    /**
     * Check whether the name of the given file matches the given pattern.
     * 
     * @param file
     * @return
     */
    public boolean isMatch(File file) {
        String name = file.getName();
        for (Iterator<Pattern> iter = hiddenFiles.iterator(); iter.hasNext();) {
            Pattern element = iter.next();
            if (element.matcher(name).matches())
                return true;
        }
        return false;
    }
}
