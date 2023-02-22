package ru.practicum.ewm.exceptions;

public class ForbiddenOperation extends RuntimeException {
    public ForbiddenOperation(String s) {
        super(s);
    }
}
