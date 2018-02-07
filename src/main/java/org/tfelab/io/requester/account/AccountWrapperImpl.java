package org.tfelab.io.requester.account;

import org.tfelab.json.JSON;

public class AccountWrapperImpl implements AccountWrapper {

	private String id;
	private String username;
	private String password;
	private String proxyId;
	private String proxyGroup;

	public AccountWrapperImpl() {}

	public AccountWrapperImpl(String id, String username, String password) {
		this.id = id;
		this.username = username;
		this.password = password;
	}

	public AccountWrapper setProxyId(String proxyId) {
		this.proxyId = proxyId;
		return this;
	}

	public AccountWrapper setProxyGroup(String proxyGroup) {
		this.proxyGroup = proxyGroup;
		return this;
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public String getUsername() {
		return this.username;
	}

	@Override
	public String getPassword() {
		return this.password;
	}

	@Override
	public String getProxyId() {
		return this.proxyId;
	}

	@Override
	public String getProxyGroup() {
		return this.proxyGroup;
	}

	@Override
	public String toJSON() {
		return JSON.toJson(this);
	}
}
