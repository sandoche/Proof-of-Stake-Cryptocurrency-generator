/*
 * Copyright © 2016-2020 Jelurida IP B.V.
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,
 * no part of this software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */

package nxt.http;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import nxt.http.APIServlet.APIRequestHandler;
import nxt.http.callers.ApiSpec;
import nxt.util.Logger;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingInt;

public class APICallGenerator {

    private static Predicate<String> ENTITY_IDENTIFIERS = exactMatch("account", "recipient", "sender", "asset",
            "poll", "account", "currency", "order", "offer", "transaction", "ledgerId", "event", "goods", "buyer",
            "purchase", "holding", "block", "ecBlockId", "setter", "recipient"
    ).or(phasingParamWhich(endsWith("Holding")));

    private static Predicate<String> INT_IDENTIFIERS = exactMatch("height", "timestamp", "firstIndex", "lastIndex",
            "type", "subtype", "deadline", "ecBlockHeight", "totalPieces", "minimumPieces", "minParticipants", "vote00", "vote01", "vote02")
            .or(phasingParamWhich(endsWith("FinishHeight")));

    private static Predicate<String> BYTE_IDENTIFIERS = exactMatch("holdingType", "votingModel", "minBalanceModel")
            .or(phasingParamWhich(endsWith("VotingModel", "MinBalanceModel", "HashedSecretAlgorithm")));


    private static Predicate<String> BOOLEAN_IDENTIFIERS = exactMatch("executedOnly", "phased", "broadcast", "voucher", "retrieve", "add", "remove", "validate")
            .or(startsWith("include", "is"))
            .or(contains("Is"));

    private static Predicate<String> LONG_IDENTIFIERS = contains("NQT", "FQT", "FXT", "QNT")
            .or(phasingParamWhich(endsWith("Quorum", "MinBalance", "Holding")))
            .or(exactMatch("timeout", "units", "counter", "minBalance", "minRangeValue", "maxRangeValue",
                    "minNumberOfOptions",
                    "maxNumberOfOptions",
                    "votingModel"
            ))
            .or(endsWith("Limit", "Supply", "Height"));

    private static Predicate<String> BYTE_ARRAYS = startsWith("fullHash", "publicKey")
            .or(contains("FullHash", "PublicKey"))
            .or(endsWith("MessageData", "Nonce", "ransactionBytes"));

    private static Predicate<String> REMOTE_ONLY_APIS = exactMatch("eventRegister", "eventWait");

    private static final String outputPackageName = "nxt.http.callers";

    private final Set<String> parametersHandledInSuperClass = new HashSet<>();

    private final String requestType;
    private final ClassName className;
    private final String typeName;
    private final TypeVariableName typeVariableName;
    private final TypeName parameterMethodReturnType;

    private APICallGenerator(String requestType) {
        this.requestType = requestType;
        this.typeName = initialCaps(requestType) + "Call";
        className = ClassName.get(outputPackageName, typeName);
        typeVariableName = null;
        parameterMethodReturnType = className;
    }

    private APICallGenerator(String requestType, String typeName) {
        this.requestType = requestType;
        this.typeName = typeName;
        className = ClassName.get(outputPackageName, typeName);
        typeVariableName = TypeVariableName.get("T", TypeName.get(APICall.Builder.class));
        parameterMethodReturnType = typeVariableName;
    }

    public static void main(String[] args) {
        generateApiSpec();
        generateApiCallers();
    }

    private static void generateApiCallers() {
        new APICallGenerator(null, "CreateTransactionCallBuilder").generateCreateTransactionCallBuilder();
        Map<String, APIRequestHandler> apiRequestHandlers = APIServlet.getAPIRequestHandlers();
        for (String requestType : apiRequestHandlers.keySet()) {
            APIRequestHandler apiRequestHandler = apiRequestHandlers.get(requestType);
            new APICallGenerator(requestType).generateApiCall(apiRequestHandler);
        }
    }

    private static void generateApiSpec() {
        // Generate the API Specification enum
        ClassName enumClass = ClassName.get(outputPackageName, "ApiSpec");
        TypeSpec.Builder apiSpec = TypeSpec.enumBuilder(enumClass).addModifiers(Modifier.PUBLIC);
        for (Map.Entry<String, APIRequestHandler> entry : APIServlet.getAPIRequestHandlers().entrySet()) {
            String requestType = entry.getKey();
            APIRequestHandler apiRequestHandler = entry.getValue();
            String fileParameter = apiRequestHandler.getFileParameter();
            CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();
            if (fileParameter != null) {
                codeBlockBuilder.add("$S", fileParameter);
            } else {
                codeBlockBuilder.add("$L", (Object) null);
            }
            codeBlockBuilder.add(", ").add("\"$L\"", String.join("\", \"", apiRequestHandler.getParameters()));
            apiSpec.addEnumConstant(requestType, TypeSpec.anonymousClassBuilder(codeBlockBuilder.build()).build());
        }

        // Generate fields and constructor
        apiSpec.addField(TypeName.get(String.class), "fileParameter", Modifier.PRIVATE, Modifier.FINAL);
        apiSpec.addField(ParameterizedTypeName.get(List.class, String.class), "parameters", Modifier.PRIVATE, Modifier.FINAL);
        CodeBlock codeBlock = CodeBlock.builder().add("this.$L = $T.asList($L);", "parameters", Arrays.class, "parameters").build();
        apiSpec.addMethod(MethodSpec.constructorBuilder().
                addParameter(TypeName.get(String.class), "fileParameter").
                addParameter(TypeName.get(String[].class), "parameters").varargs().
                addStatement("this.$L = $L", "fileParameter", "fileParameter").
                addCode(codeBlock).
                build());

        // Generate getter
        apiSpec.addMethod(MethodSpec.methodBuilder("getFileParameter")
                .addModifiers(Modifier.PUBLIC)
                .returns(String.class)
                .addStatement("return fileParameter")
                .build());
        apiSpec.addMethod(MethodSpec.methodBuilder("getParameters")
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(List.class, String.class))
                .addStatement("return parameters")
                .build());

        // Create source file
        writeToFile(apiSpec.build());
    }

    private void generateApiCall(APIRequestHandler apiRequestHandler) {
        ClassName superClass = ClassName.get(APICall.Builder.class);

        if (apiRequestHandler instanceof CreateTransaction) {
            superClass = ClassName.get(outputPackageName, "CreateTransactionCallBuilder");
            parametersHandledInSuperClass.addAll(CreateTransaction.getCommonParameters());
        }
        final TypeSpec.Builder classBuilder = TypeSpec.classBuilder(typeName)
                .addModifiers(Modifier.PUBLIC)
                .superclass(ParameterizedTypeName.get(superClass, className));


        classBuilder.addMethod(createCallerConstructor());
        classBuilder.addMethod(createFactoryMethod());

        List<String> parameters = apiRequestHandler.getParameters().stream()
                .filter(s -> !parametersHandledInSuperClass.contains(s))
                .collect(Collectors.toList());
        classBuilder.addMethods(createParameterMethods(parameters));
        classBuilder.addMethods(createFileParameterMethods(apiRequestHandler.getFileParameter()));

        classBuilder.addMethods(createIsRemoteOnly());

        writeToFile(classBuilder.build());

        String wrongParameters = parametersHandledInSuperClass.stream()
                .filter(parameter -> !apiRequestHandler.getParameters().contains(parameter))
                .collect(Collectors.joining(", "));
        if (!wrongParameters.isEmpty()) {
            Logger.logWarningMessage("Caller %s contains invalid parameter(s): %s", typeName, wrongParameters);

        }
    }

    private void generateCreateTransactionCallBuilder() {
        final TypeSpec.Builder classBuilder = TypeSpec.classBuilder(typeName)
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(typeVariableName)
                .superclass(ParameterizedTypeName.get(ClassName.get(APICall.Builder.class), typeVariableName));

        classBuilder.addMethod(createBuilderConstructor());

        List<String> parameters = CreateTransaction.getCommonParameters().stream()
                .filter(s -> !parametersHandledInSuperClass.contains(s))
                .collect(Collectors.toList());

        classBuilder.addMethods(createParameterMethods(parameters));

        writeToFile(classBuilder.build());
    }

    private MethodSpec createBuilderConstructor() {
        return MethodSpec.constructorBuilder()
                .addParameter(ApiSpec.class, "apiSpec")
                .addModifiers(Modifier.PROTECTED)
                .addStatement("super(apiSpec)")
                .build();
    }

    private static void writeToFile(TypeSpec typeSpec) {
        JavaFile javaFile = JavaFile.builder(outputPackageName, typeSpec)
                .indent("    ")
                .addFileComment("Auto generated code, do not modify")
                .skipJavaLangImports(true)
                .build();
        Path directory = Paths.get("./src/java");
        try {
            javaFile.writeTo(directory);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<MethodSpec> createIsRemoteOnly() {
        if (!REMOTE_ONLY_APIS.test(requestType)) {
            return Collections.emptyList();
        }
        MethodSpec remoteOnly = MethodSpec.methodBuilder("isRemoteOnly")
                .addModifiers(Modifier.PUBLIC)
                .returns(boolean.class)
                .addStatement("return true")
                .addAnnotation(Override.class)
                .build();
        return Collections.singletonList(remoteOnly);
    }

    private List<MethodSpec> createFileParameterMethods(String fileParam) {
        if (fileParam == null) {
            return Collections.emptyList();
        }
        MethodSpec setter = MethodSpec.methodBuilder(fileParam)
                .addModifiers(Modifier.PUBLIC)
                .returns(parameterMethodReturnType)
                .addParameter(byte[].class, "b")
                .addStatement("return parts($S, $L)", fileParam, "b").build();
        return Collections.singletonList(setter);
    }

    private List<MethodSpec> createParameterMethods(List<String> parameters) {
        List<MethodSpec> result = new ArrayList<>();

        Map<String, Integer> paramMap = parameters.stream().collect(groupingBy(Function.identity(), summingInt(e -> 1)));
        for (String paramName : paramMap.keySet()) {
            boolean isVarargs = paramMap.get(paramName) > 1;
            if (ENTITY_IDENTIFIERS.test(paramName)) {
                result.add(createMethod(paramName, "param", String.class, isVarargs));
                result.add(createMethod(paramName, "unsignedLongParam", long.class, isVarargs));
            } else if (INT_IDENTIFIERS.test(paramName)) {
                result.add(createMethod(paramName, "param", int.class, isVarargs));
            } else if (BOOLEAN_IDENTIFIERS.test(paramName)) {
                result.add(createMethod(paramName, "param", boolean.class, isVarargs));
            } else if (BYTE_IDENTIFIERS.test(paramName)) {
                result.add(createMethod(paramName, "param", byte.class, isVarargs));
            } else if (LONG_IDENTIFIERS.test(paramName)) {
                result.add(createMethod(paramName, "param", long.class, isVarargs));
            } else {
                result.add(createMethod(paramName, "param", String.class, isVarargs));
            }
            if (BYTE_ARRAYS.test(paramName)) {
                result.add(createMethod(paramName, "param", byte[].class, isVarargs));
            }
        }
        return result;
    }

    private MethodSpec createCallerConstructor() {
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .addStatement("super($T." + requestType + ")", ApiSpec.class)
                .build();
    }

    private MethodSpec createFactoryMethod() {
        MethodSpec.Builder factoryBuilder = MethodSpec.methodBuilder("create")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(className);
        factoryBuilder.addStatement("return new $L()", typeName);
        return factoryBuilder.build();
    }

    private MethodSpec createMethod(String paramName, String paramMethodName, Class paramMethodType, boolean isVarargs) {
        MethodSpec.Builder setterBuilder = MethodSpec.methodBuilder(paramName)
                .addModifiers(Modifier.PUBLIC)
                .returns(parameterMethodReturnType)
                .addStatement("return " + paramMethodName + "($S, $L)", paramName, paramName);
        if (isVarargs) {
            Class arrayClass = Array.newInstance(paramMethodType, 0).getClass();
            setterBuilder.addParameter(arrayClass, paramName).varargs();
        } else {
            setterBuilder.addParameter(paramMethodType, paramName);
        }

        return setterBuilder.build();
    }

    private static String initialCaps(String requestType) {
        return requestType.substring(0, 1).toUpperCase() + requestType.substring(1);
    }

    private static Predicate<String> startsWith(String... strings) {
        return s -> Stream.of(strings).anyMatch(s::startsWith);
    }

    private static Predicate<String> endsWith(String... strings) {
        return s -> Stream.of(strings).anyMatch(s::endsWith);
    }

    private static Predicate<String> contains(String... strings) {
        return s -> Stream.of(strings).anyMatch(s::contains);
    }

    private static Predicate<String> exactMatch(String... strings) {
        return new HashSet<>(Arrays.asList(strings))::contains;
    }

    private static Predicate<String> phasingParamWhich(Predicate<String> additionalCondition) {
        return startsWith("phasing", "control").and(additionalCondition);
    }
}
