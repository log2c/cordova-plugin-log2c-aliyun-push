import { Plugin, Cordova, IonicNativePlugin } from '@ionic-native/core';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';


interface AliyunMessageOrigin {
  /**
   *  message: 透传消息，
   *  notification: 通知接收，
   *  notificationOpened: 通知点击，
   *  notificationReceived: 通知到达，
   *  notificationRemoved: 通知移除，
   *  notificationClickedWithNoAction: 通知到达，
   *  notificationReceivedInApp: 通知到达打开 app
   */
  type: 'message' | 'notification' | 'notificationOpened' | 'notificationReceived' | 'notificationRemoved' | 'notificationClickedWithNoAction' | 'notificationReceivedInApp';
  title: string;
  content: string;
}

export interface AliyunNotification extends AliyunMessageOrigin {
  _ALIYUN_NOTIFICATION_ID_?: string;
}
export interface AliyunMessage extends AliyunMessageOrigin {
  id?: string;
  [prop: string]: any;  // 后台推送的 ExtParameters 字段,已转为 key-value 形式
}

export type Message = AliyunNotification | AliyunMessage;

/**
 * 目标类型
 * 详情参考 阿里云移动推送文档
 */
export enum AliyunPushTarget {
  DEVICE_TARGET = 1,  // 本设备
  ACCOUNT_TARGET = 2, // 本账号
  ALIAS_TARGET = 3    // 别名
}


/*
* @name Aliyun Push
* @description
* 阿里云推送
*
*
* @usage
* ```typescript
* import { AliyunPush } from '@ionic-native/aliyun-push/ngx';
* @NgModule({
*   declarations: [AppComponent],
*   entryComponents: [],
*   imports: [BrowserModule, IonicModule.forRoot(), AppRoutingModule],
*   providers: [
*     StatusBar,
*     SplashScreen,
*     { provide: RouteReuseStrategy, useClass: IonicRouteStrategy },
*     AliyunPush  <~~~~ 定义
*   ],
*   bootstrap: [AppComponent]
* })
* ```

* ```typescript
* import { AliyunPush } from '@ionic-native/aliyun-push/ngx';
* constructor(private aliyunPush: AliyunPush) { }
* ...
* this.aliyunPush.onMessage()
* .subscribe((msg) => {
*   console.log(msg);
* }, console.error);
* ```
*/


@Plugin({
  pluginName: 'AliyunPush',
  plugin: 'cordova-plugin-aliyunpush',
  pluginRef: 'AliyunPush',
  repo: 'https://github.com/log2c/cordova-plugin-aliyunpush.git',
  platforms: ['Android', 'iOS'],
  install: '',
  installVariables: ['ANDROID_APP_KEY', 'ANDROID_APP_SECRET', 'IOS_APP_KEY', 'IOS_APP_SECRET', 'HUAWEI_APPID', 'MIPUSH_APPID', 'MIPUSH_APPKEY'],
})
@Injectable()
export class AliyunPush extends IonicNativePlugin {

  @Cordova()
  getRegisterId(): Promise<string> {
    return;
  }

  @Cordova()
  bindAccount(account: string): Promise<any> {
    return;
  }

  @Cordova()
  unbindAccount(): Promise<any> {
    return;
  }

  /**
   * 阿里云推送绑定标签
   * @param tags 标签列表
   */
  @Cordova()
  bindTags(target: AliyunPushTarget, tags: string[], alias?: string): Promise<any> {
    return;
  }

  /**
   * 阿里云推送解除绑定标签
   * @param  {string[]} tags  标签列表
   */
  @Cordova()
  unbindTags(target: AliyunPushTarget, tags: string[], alias?: string): Promise<any> {
    return;
  }

  /**
   * 阿里云推送解除绑定标签
   */
  @Cordova()
  listTags(): Promise<string[]> {
    return;
  }

  /**
   * 没有权限时，请求开通通知权限，其他路过
   * @param  string msg  请求权限的描述信息
   */
  @Cordova()
  requireNotifyPermission(msg: string): Promise<any> {
    return;
  }

  /**
   * 阿里云推送消息透传回调
   */
  @Cordova({
    observable: true
  })
  onMessage(): Observable<Message> {
    return;
  }

  /**
   * 添加别名
   * @param tags 标签列表
   */
  @Cordova()
  addAlias(alias: string): Promise<any> {
    return;
  }

  /**
   * 移除别名
   * @param alias 标签列表
   */
  @Cordova()
  removeAlias(alias: string): Promise<any> {
    return;
  }

  /**
   * 查询已注册别名
   */
  @Cordova()
  listAliases(): Promise<string[]> {
    return;
  }

}
