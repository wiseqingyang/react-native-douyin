import { NativeModules } from "react-native";

const { DouYinModule } = NativeModules;

export default Douyin = {
  init(appKey) {
    DouYinModule.init(appKey);
  },

  auth(scope, state) {
    return DouYinModule.auth(scope, state);
  },

  shareVideo(config) {
    return DouYinModule.shareVideo({
      title: "",
      shortTitle: "",
      isPublish: false,
      ...config,
    });
  },

  isAppInstalled() {
    return DouYinModule.isAppInstalled();
  },
};
