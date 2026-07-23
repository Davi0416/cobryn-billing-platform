package com.cobryn.organization.domain.exception;

public class OrganizationNotFoundException extends RuntimeException {
  public OrganizationNotFoundException(String message) {
    super(message);
  }
}
