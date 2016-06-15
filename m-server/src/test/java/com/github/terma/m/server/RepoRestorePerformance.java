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
package com.github.terma.m.server;

import com.github.terma.fastselect.FastSelect;
import com.github.terma.m.shared.Event;

import java.io.File;
import java.io.IOException;

public class RepoRestorePerformance {

    public static void main(String[] args) throws IOException {
        String dataPath = new File("data").getAbsolutePath();
        Repo repo = new Repo(dataPath);
        FastSelect<Event> fastSelect = EventsImpl.createFastSelect();
        repo.readEvents(fastSelect);
        System.out.println(repo.hashCode());
        System.out.println(fastSelect.size());
    }

}
