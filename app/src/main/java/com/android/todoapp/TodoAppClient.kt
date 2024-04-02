package com.android.todoapp

import com.amazonaws.mobileconnectors.apigateway.ApiRequest
import com.amazonaws.mobileconnectors.apigateway.ApiResponse
import com.amazonaws.mobileconnectors.apigateway.annotation.Operation
import com.amazonaws.mobileconnectors.apigateway.annotation.Parameter
import com.amazonaws.mobileconnectors.apigateway.annotation.Service
import java.util.UUID


@Service(endpoint = BuildConfig.API_GATEWAY_ENDPOINT)
interface TodoappClient {
    /**
     * A generic invoker to invoke any API Gateway endpoint.
     * @param request
     * @return ApiResponse
     */
    fun execute(request: ApiRequest?): ApiResponse?

    /**
     * Gets all todos from the dynamodb todos table
     * @param idToken serves as authorization for proxy GET method
     */
    @Operation(path = "/{proxy+}", method = "GET")
    fun getAllTodos(
        @Parameter(
            name = "Authorization",
            location = "header"
        ) idToken: String?
    ): Array<Empty>

//    /**
//     * Get a specific TodoTask from dynamodb based off some values
//     * TODO fix parameter and body
//     */
//    @Operation(path = "/", method = "GET")
//    fun getTodo(@Parameter(name = "todo", location = "query") todo : String) : Empty

    /**
     * post a TodoTask to the database
     * @param todoId UUID of a TodoTask
     * @param todoTask string passed as json format of a TodoTask item
     * @param idToken serves as authorization for proxy POST method
     */
    @Operation(path = "/{proxy+}", method = "POST")
    fun postTodo(
        @Parameter(name = "Todo", location = "query") todoId: UUID, todoTask: String?,
        @Parameter(name = "Authorization", location = "header") idToken: String?
    )

    /**
     * delete a TodoTask to the database
     * @param todoId UUID of a TodoTask
     */
    @Operation(path = "/{proxy+}", method = "DELETE")
    fun deleteTodo(
        @Parameter(name = "Todo", location = "query") todoId: UUID,
        @Parameter(name = "Authorization", location = "header") idToken : String?)
}