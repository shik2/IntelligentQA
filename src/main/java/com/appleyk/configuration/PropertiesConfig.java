package com.appleyk.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.validation.Valid;

/**
 * @author Samoyed
 * @date 2019/09/04
 **/
@Component
public class PropertiesConfig {
    @Value("${aaa}")
    public String aaa;
}
