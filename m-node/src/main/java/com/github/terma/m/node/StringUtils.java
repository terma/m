/*

    Copyright 2016 Artem Stasiuk

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.github.terma.m.node;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    private StringUtils() {
        throw new UnsupportedOperationException("Just utility!");
    }

    public static String enrich(String string, Map<String, String> context) {
        Pattern pattern = Pattern.compile("\\$\\{(\\w+)\\}");

        String formatted = string;
        boolean fixed;
        do {
            fixed = false;
            Matcher matcher = pattern.matcher(formatted);
            if (matcher.find()) {
                String key = matcher.group(1);
                String replacement = context.get(key);
                if (replacement == null) replacement = "";
                formatted = matcher.replaceFirst(replacement);
                fixed = true;
            }
        } while (fixed);

        return formatted;
    }

}
