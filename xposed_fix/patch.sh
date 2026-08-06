#!/bin/bash
if [ ! -f "apktool.jar" ]; then
	wget "https://bitbucket.org/iBotPeaches/apktool/downloads/apktool_3.0.3.jar"
	mv apktool_3.0.3.jar apktool.jar
fi
if [ ! -f "base.apk" ]; then
	echo "there isn't GmsCompat Base apk"
	echo "If you hace GmsCompat it need to be named base.apk"
	exit 1
fi

if [ -d "src" ]; then
	rm -rf ./src
fi

echo "Unpacking GmsCompat"
java -jar apktool.jar d base.apk -o src

echo "Applying fix patch ..."
patch --directory=src/ -t -s -p1 < xposed_fix.patch

echo "Rebuilding APK ..."
java -jar apktool.jar b src -o patched.apk

echo "Signing APK ..."
zipalign -p 4 patched.apk patched_align.apk
apksigner sign --ks-key-alias lob --ks sign.keystore --ks-pass pass:369852 --key-pass pass:369852 --v1-signing-enabled true --v2-signing-enabled true --v3-signing-enabled true --v4-signing-enabled true patched_align.apk


rm patched.apk
rm -rf src
mv patched_align.apk fixed.apk
echo "Patching complete there is fixed.apk file for install via adb"
echo "install: adb install fixed.apk"
