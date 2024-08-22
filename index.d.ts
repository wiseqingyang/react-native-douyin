/*
 * @Author: 唉诺
 * @Date: 2021-04-30 23:31:46
 */
declare module "react-native-douyin-lib" {
  export interface IApiResponse {
    code: number;
    msg: string;
    data: any;
  }

  export function init(appKey: string):Promise<void>;

  export function auth(scope: string,state:string): Promise<any>;

  export function shareVideo(shareConfig: {
    videos: string[];
    isPublish?: boolean;
    title?: string;
    shortTitle?: string;
  }): Promise<any>;
  
  export function isAppInstalled(): Promise<IApiResponse>;
}
