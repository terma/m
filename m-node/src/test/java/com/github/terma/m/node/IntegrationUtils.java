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

public class IntegrationUtils {

    public static void runChecker(Checker checker) throws InterruptedException {
        runChecker(checker, 5000);
    }

    public static void runChecker(Checker checker, long timeout) throws InterruptedException {
        System.out.println("Run checker: " + checker);
        System.out.println("Timeout: " + timeout);

        while (!Thread.currentThread().isInterrupted()) {
            try {

                System.out.println(checker.get());
            } catch (Exception e) {
                System.out.println(e);
            }

            Thread.sleep(timeout);
        }
    }

}
