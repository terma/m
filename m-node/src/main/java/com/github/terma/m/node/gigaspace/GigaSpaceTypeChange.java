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
package com.github.terma.m.node.gigaspace;

import com.github.terma.m.node.SystemTime;

import java.util.HashMap;
import java.util.Map;

public class GigaSpaceTypeChange extends GigaSpaceType {

    private final Map<String, TimestampAndValue> previous = new HashMap<>();

    public GigaSpaceTypeChange(String host, Map<String, String> params) {
        super(host, params);
    }

    public GigaSpaceTypeChange(String host, Map<String, String> params, SystemTime systemTime) {
        super(host, params, systemTime);
    }

    @Override
    protected long calculateValue(String typeName, long value) {
        long currentTime = systemTime.getMillis();

        TimestampAndValue timestampAndValue = previous.get(typeName);
        if (timestampAndValue == null) {
            timestampAndValue = new TimestampAndValue();
            timestampAndValue.timestamp = currentTime;
            previous.put(typeName, timestampAndValue);
            return 0;
        } else {
            final long periodInSec = (currentTime - timestampAndValue.timestamp) / 1000;
            final long change = periodInSec > 0 ? (value - timestampAndValue.value) / periodInSec : 0;

            timestampAndValue.timestamp = currentTime;
            timestampAndValue.value = value;

            return change;
        }
    }

    private static class TimestampAndValue {
        long timestamp;
        long value;
    }

}
