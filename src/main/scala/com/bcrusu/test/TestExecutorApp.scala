package com.bcrusu.test

import com.bcrusu.Executor

class TestExecutorApp extends com.bcrusu.ExecutorApp {
  override def createExecutor(): Executor = new TestExecutor
}
