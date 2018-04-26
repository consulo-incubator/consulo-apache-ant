/*
 * Copyright 2000-2009 JetBrains s.r.o.
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
package com.intellij.lang.ant.config.execution;

import java.io.IOException;

import com.intellij.execution.junit.JUnitProcessHandler;
import com.intellij.execution.junit2.segments.DeferredActionsQueue;
import com.intellij.execution.junit2.segments.DeferredActionsQueueImpl;
import com.intellij.execution.junit2.segments.InputConsumer;
import com.intellij.execution.junit2.segments.OutputPacketProcessor;
import com.intellij.execution.junit2.segments.SegmentReader;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.testframework.Printable;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.lang.ant.config.AntBuildFile;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ex.MessagesEx;
import consulo.apache.ant.rt.AntLoggerConstants;

final class OutputParser2 extends OutputParser implements InputConsumer, OutputPacketProcessor {
  private static final Logger LOG = Logger.getInstance("#com.intellij.ant.execution.OutputParser2");
  private int myLastPacketIndex = -1;

  private OutputParser2(Project project,
                        OSProcessHandler processHandler,
                        AntBuildMessageView errorsView,
                        ProgressIndicator progress,
                        String buildName) {
    super(project, processHandler, errorsView, progress, buildName);
  }

  @Override
  public void processOutput(Printable printable) {
    printable.printOn(null);
  }

  public void processPacket(String packet) {
    SegmentReader reader = new SegmentReader(packet);
    int index = reader.readInt();
    if (myLastPacketIndex + 1 < index) {
      LOG.error("last: " + myLastPacketIndex + " current: " + index);
    }
    if (myLastPacketIndex + 1 > index) return;
    myLastPacketIndex++;
    char id = reader.readChar();
    if (id == AntLoggerConstants.INPUT_REQUEST) {
      try {
        InputRequestHandler.processInput(getProject(), reader, getProcessHandler());
      }
      catch (IOException e) {
        MessagesEx.error(getProject(), e.getMessage()).showLater();
      }
    }
    else {
      int priority = reader.readInt();
      char contentType = reader.readChar();
      String message = reader.readLimitedString();
      switch (id) {
        case AntLoggerConstants.BUILD_END:
          if (contentType == AntLoggerConstants.EXCEPTION_CONTENT) {
            processTag(AntLoggerConstants.EXCEPTION, message, priority);
          }
          break;
        default:
          processTag(id, message, priority);
      }
    }
  }

  public void onOutput(String text, ConsoleViewContentType contentType) {
    if (text.length() == 0) return;
    if (myLastPacketIndex != -1) return;
    if (contentType == ConsoleViewContentType.ERROR_OUTPUT) readErrorOutput(text);
  }

  public static OutputParser attachParser(final Project myProject,
                                          JUnitProcessHandler handler,
                                          final AntBuildMessageView errorView,
                                          final ProgressIndicator progress,
                                          final AntBuildFile buildFile) {
    final OutputParser2 parser = new OutputParser2(myProject, handler, errorView, progress, buildFile.getName());
    final DeferredActionsQueue queue = new DeferredActionsQueueImpl();
    handler.getErr().setPacketDispatcher(parser, queue);
    return parser;
  }
}
