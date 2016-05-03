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

import java.util.concurrent.TimeUnit;

public class JvmStability {

    public static void main(String[] args) throws Exception {
        Checker checker = new Jvm("localhost", "marker=(JVM_STABILITY)");
        final long duration = TimeUnit.MINUTES.toMillis(5);
        final long start = System.currentTimeMillis();

        long hole = 0;

        new Thread(new Runnable() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Thread.sleep(TimeUnit.SECONDS.toMillis(10));
                        System.out.println("Live " + ((System.currentTimeMillis() - start) / 1000) + " sec");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        }).start();

        while (System.currentTimeMillis() - start < duration) {
            hole = Math.max(checker.get().hashCode(), hole);
        }
        System.out.println("Nothing: " + hole);
    }

}
