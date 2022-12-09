package io.github.jmmedina00.adoolting.exception;

public class AlreadyInPlaceException extends Exception {
  private Long pageId;

  public AlreadyInPlaceException(Long pageId) {
    this.pageId = pageId;
  }

  public Long getPageId() {
    return pageId;
  }

  public void setPageId(Long pageId) {
    this.pageId = pageId;
  }
}
