/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
var app = {
    // Application Constructor
    initialize: function() {
        document.addEventListener('deviceready', this.onDeviceReady.bind(this), false);
    },

    // deviceready Event Handler
    //
    // Bind any cordova events here. Common events are:
    // 'pause', 'resume', etc.
    onDeviceReady: function() {
        this.receivedEvent('deviceready');

        ETCLocation.saveOffLineData(function(mfilePath){
            alert(mfilePath);

            //读取离线文件内容
            ETCLocation.readOffLineData(function(content){
                alert('读取内容: '+content);
            },function(err){
            },{
                filePath:mfilePath
            });


                  //复制文件
             ETCLocation.getSavePathByTaskType(function(rootPath){
                    alert('rootPath: '+rootPath);
             },function(err){
             },{
                taskType:'aaa',
             });

            //复制文件
            ETCLocation.copyFile(function(isCopyComplate){
                alert('文件是否复制成功: '+isCopyComplate);
            },function(err){
            },{
                fromFilePath:mfilePath,
                toFilePath:'/storage/emulated/0/Android/data/com.talkweb.tollmanage/files/copy.txt'
            });
            
        },function(err){
        },{
            taskType:'aaa',
            content:JSON.stringify({a:123,b:123})
        });



        ETCLocation.getFileListByTaskType(function(filePathList){
            alert('文件列表个数: '+filePathList.length);
        },function(err){
        },{
            taskType:'aaa111'
        });


        alert(0);

        ETCLocation.getDeviceId(function(deviceId){
            alert(deviceId);
        },function(err){
        });

        alert(1);
        //保存扩展信息
        ETCLocation.saveExtendData(function(success){
        },function(err){

        },{
            a:123,
            userName:'李四',
        });
        
        alert(2);
        // ETCLocation.deleteExtendData(function(success){
               
        // },function(err){

        // });
        
        // alert(3);

       //启动后台
       ETCLocation.startBackgroundLocation(function(success){
        alert(success)
        },function(err){
            alert(err)
        },{
            extendParams:'{"a":"aaaaa"}'
        })
    },

    // Update DOM on a Received Event
    receivedEvent: function(id) {
        var parentElement = document.getElementById(id);
        var listeningElement = parentElement.querySelector('.listening');
        var receivedElement = parentElement.querySelector('.received');

        listeningElement.setAttribute('style', 'display:none;');
        receivedElement.setAttribute('style', 'display:block;');

        console.log('Received Event: ' + id);
    }
};

app.initialize();