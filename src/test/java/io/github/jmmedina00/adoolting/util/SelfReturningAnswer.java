package io.github.jmmedina00.adoolting.util;

import static org.mockito.Mockito.RETURNS_DEFAULTS;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class SelfReturningAnswer implements Answer<Object> {

  // https://stackoverflow.com/questions/8501920/how-to-mock-a-builder-with-mockito

  @Override
  public Object answer(InvocationOnMock invocation) throws Throwable {
    Object mock = invocation.getMock();
    return invocation.getMethod().getReturnType().isInstance(mock)
      ? mock
      : RETURNS_DEFAULTS.answer(invocation);
  }
}
