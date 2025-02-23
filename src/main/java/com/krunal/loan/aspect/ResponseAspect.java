package com.krunal.loan.aspect;

import com.krunal.loan.repository.UserRepository;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.List;

@Aspect
@Component
public class ResponseAspect {

    private static final Logger logger = LoggerFactory.getLogger(ResponseAspect.class);
    private final UserRepository userService;

    public ResponseAspect(UserRepository userService) {
        this.userService = userService;
    }

    @Pointcut("@annotation(com.krunal.loan.aspect.AddUserNames)")
    public void addUserNamesPointcut() {
    }

    @AfterReturning(pointcut = "addUserNamesPointcut()", returning = "result")
    public void addUpdateUserName(Object result) {
        if (result instanceof ResponseEntity<?> responseEntity) {
            handleResponseEntity(responseEntity);
        } else if (result instanceof List<?> list) {
            handleList(list);
        } else {
            handleSingleObject(result);
        }
    }

    private void handleResponseEntity(ResponseEntity<?> responseEntity) {
        Object body = responseEntity.getBody();
        if (body != null) {
            if (body instanceof List<?> list) {
                handleList(list);
            } else {
                addUpdateUserNameToObject(body);
            }
        }
    }

    private void handleList(List<?> list) {
        if (!list.isEmpty()) {
            for (Object item : list) {
                addUpdateUserNameToObject(item);
            }
        }
    }

    private void handleSingleObject(Object result) {
        if (result != null) {
            addUpdateUserNameToObject(result);
        }
    }

    private void addUpdateUserNameToObject(Object obj) {
        try {
            Long updatedUserId = getFieldValue(obj, "updatedUser");
            if (updatedUserId != null) {
                addUserNameToObject(obj, updatedUserId, "updateUserName");
            }

            Long addUserId = getFieldValue(obj, "addUser");
            if (addUserId != null) {
                addUserNameToObject(obj, addUserId, "addUserName");
            }
        } catch (IllegalAccessException e) {
            logger.error("Error adding user names to object of class '{}'", obj.getClass().getSimpleName(), e);
        }
    }

    private Long getFieldValue(Object obj, String fieldName) throws IllegalAccessException {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return (Long) field.get(obj);
        } catch (NoSuchFieldException e) {
            logger.warn("Field '{}' not found in class '{}'", fieldName, obj.getClass().getSimpleName());
            return null;
        }
    }

    private void addUserNameToObject(Object obj, Long userId, String userNameField) {
        userService.findById(userId).ifPresent(user -> {
            String userName = user.getName();
            try {
                Field field = obj.getClass().getDeclaredField(userNameField);
                field.setAccessible(true);
                field.set(obj, userName);
                logger.info("Added {} '{}' to object of class '{}'", userNameField, userName, obj.getClass().getSimpleName());
            } catch (NoSuchFieldException e) {
                logger.warn("Field '{}' not found in class '{}'", userNameField, obj.getClass().getSimpleName());
            } catch (IllegalAccessException e) {
                logger.error("Error adding {} to object of class '{}'", userNameField, obj.getClass().getSimpleName(), e);
            }
        });
    }
}