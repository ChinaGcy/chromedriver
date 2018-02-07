package org.tfelab.io.requester.account;

import org.tfelab.json.JSONable;

import java.io.Serializable;

public interface AccountWrapper extends JSONable, Serializable {

	public String getId();

	public String getUsername();

	public String getPassword();

	public String getProxyId();

	public String getProxyGroup();

}
