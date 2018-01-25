package org.apereo.cas.authentication.handler;

import org.apereo.cas.authentication.handler.PrincipalNameTransformer;

import jregex.Pattern;
import jregex.Replacer;

public class RegexPrincipalNameTransformer implements PrincipalNameTransformer {

	private String regex = null;
	private String replace = "";
	
	@Override
	public String transform(String name) {
		if(null == regex || regex.isEmpty()) {
			return name;
		}
		Pattern p = new Pattern(regex);
		Replacer r = p.replacer(replace);
		String result = r.replace(name);
		return result;
	}

	public String getRegex() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}

	public String getReplace() {
		return replace;
	}

	public void setReplace(String replace) {
		this.replace = replace;
	}

}
