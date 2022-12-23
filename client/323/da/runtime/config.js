// ====== LAUNCHER CONFIG ====== //
var config = {
    dir: "McSkill", // Launcher directory
	dirwin: "AppData\\Roaming\\McSkill",
	title: "McSkill", // Window title
	icons: [ "favicon.png" ], // Window icon paths

	// Auth config
	newsURL: "https://launcher.mcskill.net/", // News WebView URL
	linkText: "Забыли пароль?", // Text for link under "Auth" button
    linkURL: new java.net.URL("https://mcskill.net/?page=register"), // URL for link under "Auth" button
	linkResetPasswd: new java.net.URL("https://mcskill.net/?page=lostpass"),
	linkSite: new java.net.URL("https://mcskill.net/"),
	linkDiscord: new java.net.URL("https://discord.su/mcskill"),
	linkVK: new java.net.URL("https://vk.com/mcskill"),
	buyURL: new java.net.URL("https://mcskill.net/pay"),
	transferURL: new java.net.URL("https://mcskill.net/emeralds"),
	
    // Settings defaults
	settingsMagic: 0xC0DE5, // Ancient magic, don't touch
	autoEnterDefault: false, // Should autoEnter be enabled by default?
	fullScreenDefault: false, // Should fullScreen be enabled by default?
	ramDefault: 2048, // Default RAM amount (0 for auto)

	// Custom JRE config (!!! DON'T CHANGE !!!)
    jvmMustdie32Dir: "jre-8u202-win32", jvmMustdie64Dir: "jre-8u202-win64",
    jvmLinux32Dir: "jre-8u202-linux32", jvmLinux64Dir: "jre-8u202-linux64",
    jvmMacOSXDir: "jre-8u202-macosx", jvmUnknownDir: "jre-8u202-unknown"
};

// ====== DON'T TOUCH! ====== //
var dir = IOHelper.HOME_DIR.resolve(config.dir);
switch (JVMHelper.OS_TYPE) {
	case JVMHelperOS.MUSTDIE: dir = IOHelper.HOME_DIR.resolve(config.dirwin); break;
	default: dir = IOHelper.HOME_DIR.resolve(config.dir); break;
}

if (!IOHelper.isDir(dir)) {
	java.nio.file.Files.createDirectory(dir);
}
var defaultUpdatesDir = dir.resolve("updates");
if (!IOHelper.isDir(defaultUpdatesDir)) {
	java.nio.file.Files.createDirectory(defaultUpdatesDir);
}
