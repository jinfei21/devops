package com.yjfei.antibot.util;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.utility.RandomString;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class Util {


public  static   String variableReplace(String line, Map<String, String> map) {

		Pattern pattern = Pattern.compile("\\{\\{([a-zA-Z0-9_-]+)\\}\\}");
		String newLine = line;
		Matcher matcher = pattern.matcher(newLine);
		while (matcher.find()) {
			String key = matcher.group(1);
			String value = Optional.ofNullable(map.get(key)).orElseThrow(() -> new RuntimeException(key + " is null."));
			line = line.replace("{{" + key + "}}", value);
		}
		return line;
	}

private static Pattern IF_PATTERN = Pattern.compile("\\{\\{\\s+if\\s+([a-z]+)\\s+([a-zA-Z0-9_-]+)\\s+'([a-zA-Z0-9_-]+)'\\s+\\}\\}");

public  static  boolean checkIf(String line) {
		Matcher matcher = IF_PATTERN.matcher(line);
		return matcher.find() && matcher.groupCount() == 3;
	}

public  static  boolean compareIf(String line, Map<String, String> map) {
		Matcher matcher = IF_PATTERN.matcher(line);

		if (matcher.find() && matcher.groupCount() == 3) {
			String op = matcher.group(1);
			String variable = map.getOrDefault(matcher.group(2), "");
			String constant = matcher.group(3);

			if (op.equals("eq")) {
				return variable.equals(constant);
			} else if (op.equals("ne")) {
				return !variable.equals(constant);
			}
		}

		return false;
	}

public  static  boolean checkElse(String line) {
		Pattern pattern = Pattern.compile("\\{\\{\\s+else+\\s\\}\\}");
		Matcher matcher = pattern.matcher(line);
		return matcher.find();
	}

public  static  boolean checkEnd(String line) {
		Pattern pattern = Pattern.compile("\\{\\{\\s+end+\\s\\}\\}");
		Matcher matcher = pattern.matcher(line);
		return matcher.find();
	}

	// TODO : Only 1 level if statement is supported.
public  static List<String> processCondition(List<String> source, Map<String, String> map) {
		List<String> output = new ArrayList<>();
		boolean startIf = false;
		boolean startElse = false;

		boolean copyUntilElse = false;
		boolean copyFromElse = false;

		for (String line : source) {

			if (checkIf(line)) {
				startIf = true;
				if (compareIf(line, map)) {
					copyUntilElse = true;
				} else {
					copyFromElse = true;
				}
				continue;
			}

			if (startIf) {
				// check 'end' or 'else'
				if (checkEnd(line)) {
					startIf = false;
					startElse = false;
					copyUntilElse = false;
					copyFromElse = false;
				} else if (checkElse(line)) {
					startElse = true;
				} else if (!startElse && copyUntilElse) {
					output.add(line);
				} else if (startElse && copyFromElse) {
					output.add(line);
				}
			}
			// if-statement is not started...
			else {
				output.add(line);
			}
		}
		return output;
	}

 public static List<String> processTransform(List<String> source, Map<String, String> map) {
		List<String> output = new ArrayList<>();
		for (String line : source) {
			output.add(variableReplace(line, map));
		}
		return output;
	}

 public static String getDataFromTemplate(String resourcePath, Map<String, String> map) throws IOException {
		List<String> buffer = new ArrayList<>();
		InputStream resource = new ClassPathResource(resourcePath).getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(resource));

		String line = reader.readLine();

		while (line != null) {
			buffer.add(line);
			line = reader.readLine();
		}

		buffer = processCondition(buffer, map);
		buffer = processTransform(buffer, map);

		String result = String.join("\n", buffer);
		log.info(result);
		return result;
	}

 public static InputStream getInputStreamFromTemplate(String resourcePath, Map<String, String> map) throws IOException {
		String data = getDataFromTemplate(resourcePath, map);
		return new ByteArrayInputStream(data.getBytes());
	}

 public static String issueRequestId() {
		return RandomStringUtils.random(10, true, false);
	}

 public static String sanitizeString(String str) {
		return str.replace("_", "-").replace(".", "-").replace("/", "-").toLowerCase();
	}

 public static String sanitizeAppName(String appName) {
		appName = sanitizeString(appName);
		if (appName.length() > 53) {
			appName = appName.substring(0, 53);
		}
		while (appName.length() > 0 && appName.endsWith("-")) {
			appName = appName.substring(0, appName.length() - 1);
		}
		return appName;
	}

	static public long currentTimestamp() {
		return System.currentTimeMillis();
	}

	static public long expiryTimestamp() {
		return currentTimestamp() + 1 * 24 * 60 * 60 * 1000;
	}

}
