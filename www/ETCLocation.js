//
//  VideoEditor.js
//
//  Created by Josh Bavari on 01-14-2014
//  Modified by Ross Martin on 01-29-15
//

var exec = require('cordova/exec');
var pluginName = 'ETCLocation';

function ETCLocation() {}


/**
 * 启动后台上传服务
 * 
 * options ={
 * extendParams: JSON.stringify(options.extendParams) ||'{}' //扩展参数
 * }   
 */
ETCLocation.prototype.startBackgroundLocation = function(success, error, options) {

  // if(!options.serverURL){
  //   alert('服务上传接口不能为空');
  //   return ;
  // }

  var parmas={
    extendParams: JSON.stringify(options.extendParams) ||'{}' //扩展参数
  }
  exec(success, error, pluginName, 'startBackgroundLocation', [parmas]);
};
 

/**
 * 保存扩展信息
 * }   
 */
ETCLocation.prototype.saveExtendData = function(success, error, dataObj) {
  exec(success, error, pluginName, 'saveExtendData', [dataObj]);
};


/**
 * 删除扩展信息
 * }   
 */
ETCLocation.prototype.deleteExtendData = function(success, error) {
  exec(success, error, pluginName, 'deleteExtendData', []);
};
 


/**
 * 获取设备ID (指纹)
 */
ETCLocation.prototype.getDeviceId = function(success, error) {
  exec(success, error, pluginName, 'getDeviceId', []);
};


/**
 * 保存离线文件
 */
ETCLocation.prototype.saveOffLineData = function(success, error,options) {
  var parmas={
    taskType:options.taskType||'default',
    content:options.content||''
  }
  exec(success, error, pluginName, 'saveOffLineData', [parmas]);
};


/**
 * 读取离线文件
 */
ETCLocation.prototype.readOffLineData = function(success, error,options) {
  var parmas={
    filePath:options.filePath||''
  }
  exec(success, error, pluginName, 'readOffLineData', [parmas]);
};


/**
 * 获取TaskType下所有文件列表
 */
ETCLocation.prototype.getFileListByTaskType = function(success, error,options) {
  var parmas={
    taskType:options.taskType||'default',
  }
  exec(success, error, pluginName, 'getFileListByTaskType', [parmas]);
};


/**
 * 复制文件
 */
ETCLocation.prototype.copyFile = function(success, error,options) {
  var parmas={
    fromFilePath:options.fromFilePath||'',
    toFilePath:options.toFilePath||''
  }
  exec(success, error, pluginName, 'copyFile', [parmas]);
};

/**
 * 获取taskType Root路径
 */
ETCLocation.prototype.getSavePathByTaskType = function(success, error,options) {
  var parmas={
    taskType:options.taskType||'',
  }
  exec(success, error, pluginName, 'getSavePathByTaskType', [parmas]);
};



ETCLocation.prototype.deleteFile = function(success, error,options) {
  var parmas={
    filePath:options.filePath||''
  }
  exec(success, error, pluginName, 'deleteFile', [parmas]);
};







module.exports = new ETCLocation();
