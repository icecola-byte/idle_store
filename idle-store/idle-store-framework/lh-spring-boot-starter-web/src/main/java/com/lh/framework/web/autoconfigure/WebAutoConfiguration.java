package com.lh.framework.web.autoconfigure;

import com.lh.framework.web.exception.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(GlobalExceptionHandler.class)
public class WebAutoConfiguration {

}
