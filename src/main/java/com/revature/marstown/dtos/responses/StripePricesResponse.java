package com.revature.marstown.dtos.responses;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StripePricesResponse {
    private String object;
    private List<StripePriceResponse> data;
    private Map<String, StripePriceResponse> map;
    private boolean has_more;
    private String url;
}
