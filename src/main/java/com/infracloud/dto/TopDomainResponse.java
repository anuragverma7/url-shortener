package com.infracloud.dto;

public record TopDomainResponse(String domain,
        long count) implements Comparable<TopDomainResponse>{
	@Override
    public int compareTo(TopDomainResponse o) {
        return Long.compare(o.count, this.count); // descending
    }
}
