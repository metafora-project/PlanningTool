package de.kuei.metafora.server.planningtool.xml;

public class PlanningToolException extends Exception
{
    public PlanningToolException()
    {
    }

    public PlanningToolException(String message)
    {
        super(message);
    }

    public PlanningToolException(Throwable throwable)
    {
        super(throwable);
    }

    public PlanningToolException(String message, Throwable throwable)
    {
        super(message, throwable);
    }
}
