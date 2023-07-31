package com.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.HashMap;


/**
 * Example class that demonstrates how to unit test UDFs.
 */
public class FormulaUdfTests {

  @Test
  void testFormula() {
    final FormulaUdf formulaUdf = new FormulaUdf();
    Map<String, String> configurationMap = new HashMap<>();
    configurationMap.put("ksql.functions.formula.base.value", "5");
    formulaUdf.configure(configurationMap);
    assertEquals(59, formulaUdf.formula(6, 9));
  }


  @Test
  void testFormulaWillFail() {
    final FormulaUdf formulaUdf = new FormulaUdf();
    Map<String, String> configurationMap = new HashMap<>();
    configurationMap.put("ksql.functions.formula.base.value", "5");
    formulaUdf.configure(configurationMap);
    assertEquals(59, formulaUdf.formula(6, 1));
  }

}
