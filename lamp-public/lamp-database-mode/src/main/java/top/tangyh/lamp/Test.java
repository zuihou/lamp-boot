package top.tangyh.lamp;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;

/**
 * @author tangyh
 * @version v1.0
 * @date 2022/8/16 8:43 PM
 * @create [2022/8/16 8:43 PM ] [tangyh] [初始创建]
 */
public class Test {
    static String path = "/Users/tangyh/gitlab/lamp-web-pro/src/locales/lang/zh-CN/component.ts";

    public static void main(String[] args) {
        File folder = new File(path);

        aaa(folder);
    }

    static String SLOT_PAT = "([a-zA-Z0-9_]*): '(.*)'";

    public static void aaa(File folder) {
        File[] files = folder.listFiles();
        if (files == null) {
            sss(folder);
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                aaa(file);
                continue;
            }
            sss(file);
        }
    }

    private static void sss(File file) {

        String name = file.getName();
        System.out.println(name);
        if (name.endsWith(".ts")) {
            String newName = StrUtil.subBefore(name, ".ts", true);
            File target = new File(file.getParent() + "/" + newName + ".json");

            System.out.println(file.getAbsoluteFile());
            System.out.println(target.getAbsoluteFile());
            FileUtil.move(file, target, true);

            // 读取
            String con = FileUtil.readString(target, StandardCharsets.UTF_8);
            // 替换
            String text = StrUtil.replace(con, "export default {", "{");
            text = StrUtil.replace(text, ",\n" +
                    "};", "\n" +
                    "}");
            text = StrUtil.replace(text, "table: {", "\"table\": {");
            text = StrUtil.replace(text, SLOT_PAT, matcher -> getText(con, matcher));
            // 写入
            FileUtil.writeString(text, target, StandardCharsets.UTF_8);
            System.out.println(text);
        }
    }


    private static String getText(String con, Matcher matcher) {
        String key = StrUtil.trim(matcher.group(1));
        String value = StrUtil.trim(matcher.group(2));
        return "\"" + key + "\": \"" + value + "\"";
    }
}
