package com.triage.api.dto;

import java.util.List;

public record PageResponse<T>(List<T> items, Long nextCursor) {}
