/*
 * Copyright 2019 Naver Corp. All rights Reserved.
 * Naver PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package webserver;

import java.io.InputStream;

public class HttpRequest {
	private InputStream in;

	public HttpRequest(InputStream in) {
		this.in = in;
	}

	public String getMethod() {
		return null;
	}

	public String getPath() {
		return null;
	}

	public String getHeader(String connection) {
		return null;
	}

	public String getParameter(String userId) {
		return null;
	}
}
