/**
 * Copyright 2016 JustWayward Team
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.is.androidServer.wifitransfer;


import com.is.androidServer.api.BookApi;

import java.io.IOException;

/**
 * Wifi传书 服务端
 *
 * @author yuyh.
 * @date 2016/10/10.
 */
public class ServerRunner {

    private static SimpleFileServer server;
    public static boolean serverIsRunning = false;

    /**
     * 启动wifi传书服务
     * @param mBookApi
     */
    public static SimpleFileServer  startServer( BookApi mBookApi) {
        server = SimpleFileServer.getInstance();
        server.setBookApi(mBookApi);
        try {
            if (!serverIsRunning) {
                server.start();
                serverIsRunning = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return server;
    }

    public static void stopServer() {
        if (server != null) {
            server.stop();
            serverIsRunning = false;
        }
    }
}