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
 * 按指定长度分段字符串
 * @param str 传入的字符串(非空)
 * @param num 指定长度(正整数)
 * @returns Array(字符串数组)
 */
function fixedLengthFormatString(str,num){
  if(str == null || str == undefined) return null;
  if(!(/^[0-9]*[1-9][0-9]*$/.test(num))) return null;
  var array = new Array();
  var len = str.length;
  for(var i=0;i<(len/num);i++){
    if((i+1)*num > len){
    array.push(str.substring(i*num,len));
}else{
    array.push(str.substring(i*num,(i+1)*num));
}
  }
  return array;
}



/**
 * 保存离线文件
 */
ETCLocation.prototype.saveOffLineData = async function(success, error,options) {

  var content = options.content||'';
  if(!content){
    alert('保存内容长度为0')
    return;
  }

  var maxSpliceLength = 1024*5;  //最大分割字符数  5k 保存一次
  var fixedArray = fixedLengthFormatString(content,maxSpliceLength);

  var isSuccess = true;
  var mfilePath = '';
  for(var j = 0,len = fixedArray.length; j < len; j++){
    var parmas={
      taskType:options.taskType||'default',
      content:fixedArray[j],
      fileName:options.fileName||'',
      isAppend:j===0?false:true,
    }
    await execSync(function(filePath){
      mfilePath =filePath;
    },function(err){
      isSuccess=false
    },pluginName, 'saveOffLineData', [parmas]);
  }

  if(isSuccess){
    success(mfilePath);
  }else{
    error('save fail');
  }
};

function execSync(success,fail,pluginName,method,paramArray){
  return new Promise(function(resolve) {
    exec(function(retVal){
      success(retVal);
      resolve(retVal);
    }, function(err){
      fail(err);
      resolve(err);
    }, pluginName, method, paramArray);
  });
}

function readDataBySync(parmas){
  return new Promise(function(resolve) {
    let fullContent = '';
    exec(function(content){

      if(content==='@@@end_content@@@'){
        resolve(fullContent)
      }
      fullContent+=content;
    }, function(err){
    }, pluginName, 'readOffLineData', [parmas]);
  });
}

/**
 * 读取离线文件
 */
ETCLocation.prototype.readOffLineData = async function(success, error,options) {
  var parmas={
    filePath:options.filePath||''
  }

  //结果也是分批次回来 
  let fullContent = await readDataBySync(parmas);
  success(fullContent);
  
  // exec(success, error, pluginName, 'readOffLineData', [parmas]);
};


/**
 * 读取离线文件 根据TaskType类型读取文件
 */
ETCLocation.prototype.readOffLineDataByTaskType = function(success, error,options) {
  var parmas={
    taskType:options.taskType||'default',
    fileName:options.fileName||''
  }
  exec(success, error, pluginName, 'readOffLineDataByTaskType', [parmas]);
};



/**
 * 获取TaskType下所有文件列表
 */
ETCLocation.prototype.getFileListByTaskType = function(success, error,options) {
  var parmas={
    taskType:options.taskType||'default',
    extension:options.extension||'.d',
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


/**
 * 获取目录大小
 */
ETCLocation.prototype.getTaskFolderSize = function(success, error,options) {
  var parmas={
    taskType:options.taskType||'',
  }
  exec(success, error, pluginName, 'getTaskFolderSize', [parmas]);
};



/**
 * 删除目录
 */
ETCLocation.prototype.deleteTaskFolder = function(success, error,options) {
  var parmas={
    taskType:options.taskType||'',
  }
  exec(success, error, pluginName, 'deleteTaskFolder', [parmas]);
};





module.exports = new ETCLocation();
