import { NativeModules } from "react-native";

const { DouYinModule } = NativeModules;

export default Douyin = {
  init(appKey) {
    DouYinModule.init(appKey);
  },
  auth(scope, state) {
    return DouYinModule.auth(scope, state);
  },
  share(path, publish) {
    return DouYinModule.shareVideo(path, publish);
  },
  isAppInstalled() {
    return DouYinModule.isAppInstalled();
  },
};
