package it.jrc.form;


import it.jrc.form.AdminStringUtil;

import java.util.Arrays;
import java.util.Collection;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class AdminStringUtilTests {

  @Parameters
  public static Collection<Object[]> generateData()
  {
     return Arrays.asList(new Object[][] {
        { "lowercase", "Lowercase" },
        { "lowerCamelCase", "Lower Camel Case" },
        { "UpperCamelCase", "Upper Camel Case" },
        { "Upper1Camel1Case1", "Upper 1 Camel 1 Case 1" },
     });
  }


  private String source;
  private String expected;


  public AdminStringUtilTests(String source, String expected) {

    this.source = source;
    this.expected = expected;

  }

  @Test
  public void test() {

    Assert.assertEquals(expected, AdminStringUtil.splitCamelCase(source));

  }

}

