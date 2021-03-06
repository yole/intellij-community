/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.xml.actions.validate;

import com.intellij.xml.util.XmlResourceResolver;
import org.xml.sax.SAXParseException;

import java.util.ArrayList;
import java.util.List;

public class TestErrorReporter extends ErrorReporter {
  private final ArrayList<String> errors = new ArrayList<String>(3);

  public TestErrorReporter(ValidateXmlActionHandler handler) {
    super(handler);
  }

  public boolean isStopOnUndeclaredResource() {
    return true;
  }

  public boolean filterValidationException(final Exception ex) {
    if (ex instanceof XmlResourceResolver.IgnoredResourceException) throw (XmlResourceResolver.IgnoredResourceException)ex;
    return errors.add(ex.getMessage());
  }

  public void processError(SAXParseException ex, ValidateXmlActionHandler.ProblemType warning) {
    errors.add(myHandler.buildMessageString(ex));
  }

  public List<String> getErrors() {
    return errors;
  }
}
