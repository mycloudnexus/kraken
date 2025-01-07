# Property validation in Kraken data plane

## Table of Contents
1. [Introduction](#introduction)
2. [Glossary](#glossary)
3. [Overview](#overview)
4. [Validation Configurations](#validation-configurations)
5. [Validation of existence for one request property](#validation-of-existence-for-one-request-property)
6. [Validation of specific value for one request property](#validation-of-specific-value-for-one-request-property)
7. [Validation of range value for one request property](#validation-of-range-value-for-one-request-property)
8. [Validation of dynamic parameters](#validation-of-dynamic-parameters)
9. [Validation of routing parameters](#validation-of-routing-parameters)
10. [Testing the validation rules](#testing-the-validation-rules)

## Introduction
The validation of properties in Kraken aims to guarantee the legality of the request input 
and make sure all the requests at the gateway side pass the checking rules corresponding to the regulations defined by MEF.
Key steps in the Spring Cloud Gateway request handling process:

- Incoming request

The client sends an HTTP request to the Spring Cloud Gateway.
- Route matching

The "Gateway Handler Mapping" analyzes the request using configured route predicates to determine which route the request should be routed to.
- Filter chain execution (pre-filters)

If a matching route is found, the "Gateway Web Handler" takes over and executes a chain of filters marked as "pre" filters, 
allowing for modifications to the request before it is forwarded to the backend service.
- Proxy request

The modified request is then proxied to the target backend service specified in the matched route.
- Backend response

The backend service sends a response back to the Spring Cloud Gateway.
- Filter chain execution (post-filters)

The response goes through another filter chain, this time with "post" filters, 
which can further modify the response before sending it back to the client.

## Glossary

- Predicates

These are used to define conditions that must be met for a route to match a request, 
such as checking the request path, headers, or query parameters.
- Filters

Filters are custom logic that can be applied to a request before or after it is forwarded to the backend service, 
allowing for tasks like authentication, logging, rate limiting, and response modification.
- Reactive programming

The Kraken data plane is based on the Spring Cloud Gateway that is built on top of Spring WebFlux, 
which utilizes reactive programming to handle requests asynchronously and efficiently.

## Overview
To simplify the validation process, here we only consider the legal HTTP requests that stepped into  'KrakenGatewayFilterSpecFunc' 
and 'ActionGatewayFilterFactory'in the Kraken data plane. 
All the steps illustrated in the below graph are abstracted as 'runner' which is the basic orchestration unit 
and the validation rules are executed in the 'MappingMatrixCheckerActionRunner' class one by one.
![validation-sequence](docs/img/validation-sequence.png)

## Validation Configurations
All the static validation rules are put under the folder with path 'kraken-open/kraken-java-sdk/kraken-java-sdk-mef/src/main/resources/mef-sonata/mapping-matrix', 
while the dynamic validation rules lie in the target mapper files which will be executed before the rule items configured in matrix files for each target file key.

## Validation of existence for one request property
```
- name: productOrderItem[0].product.place[0].@type
  path: "$.body.productOrderItem[0].product.place[0].@type"
  expectType: EXPECTED_EXIST
  value: true
```
In the above example, the 'MappingMatrixCheckerActionRunner' checks whether the value that the path indicated exists or not, 
if it  doesn't exist, an exception will be thrown immediately with HTTP code 422. 
The error message could be customized by appending the item 'errorMsg: the error message you expect here'.

e.g.
```
- name: productOrderItem[0].product.place[0].@type
  path: "$.body.productOrderItem[0].product.place[0].@type"
  expectType: EXPECTED_EXIST
  value: true
  errorMsg: the error message you expect here
```
The checking item value is default with value ‘true' which means that this item checking is opening, not closing. 
If it’s closing, an exception will be thrown as well and the error message can be personalized like the following:
e.g.
```
- name: productOrderItem[0].product.place[0].@type
  path: "$.body.productOrderItem[0].product.place[0].@type"
  expectType: EXPECTED_EXIST
  value: false
  code: 400
  errorMsg: the error message you expect here
```

## Validation of specific value for one request property

```
- name: productOrderItem[0].product.place[0].@type
  path: "$.body.productOrderItem[0].product.place[0].@type"
  expectType: EXPECTED
  value: "GeographicAddressRef"

```

The sample depicted above shows how the constant value checking works.
The ‘MappingMatrixCheckerActionRunner' verifies the equality of the path’s value and the ‘GeographicAddressRef' assigned in the value item.
Assuming that the ‘@type’ in the request body is not 'GeographicAddressRef’, then the runner throws an exception with 400 HTTP status code.

## Validation of range value for one request property
The enumeration type of property will be checked automatically if it’s defined a the request section in the target mapper files.
```
- name: mapper.order.uni.add.duration.units
  title: "order item Term unit"
  source: "@{{productOrderItem[0].requestedItemTerm.duration.units}}"
  target: ""
  sourceType: enum
  sourceValues:
    - calendarYears
    - calendarMonths
    - calendarDays
    - calendarHours
    - calendarMinutes
    - businessDays
    - businessHours
    - businessMinutes
  sourceLocation: BODY
  targetLocation: BODY
  valueMapping: {}
  requiredMapping: true
```

## Validation of dynamic parameters

```
- name: address_submittedGeographicAddress_country
  path: "$.body.submittedGeographicAddress.['country']"
  expectType: EXPECTED_TRUE
  value: "${param}"
  errorMsg: "submittedGeographicAddress.country can not be null"
  code: 400
```

Sometimes we only consider whether the parameter has value or not but are not concerned about its specific value because we don’t know what the actual value will be. 
In this situation, the expected type of 'EXPECTED_TRUE' may be considered as a better choice. 
Another equivalent style can be written as the following:
```
- name: address_submittedGeographicAddress_country
  path: "$.body.submittedGeographicAddress.['country']"
  expectType: EXPECTED_EXIST
  value: true
  errorMsg: "submittedGeographicAddress.country can not be null"
  code: 400
```

The ‘EXPECTED_TRUE' type is designed to be used for checking the items in an array. 
For instance, the following rule checks the existence of the ‘emailAddress’ under the 'relatedContactInformation’ array:
```
- name: relatedContactInformation_emailAddress
  path: "$.body.relatedContactInformation[*]"
  expectType: EXPECTED_TRUE
  value: ${param.emailAddress}
  errorMsg: "relatedContactInformation.emailAddress should exist in request"
```
The related request body looks like the following:
```
{
    "relatedContactInformation": [
        {
            "number": "111 222 333",
            "emailAddress": "productOrderContact@mef.com",
            "role": "productOrderContact",
            "postalAddress": null,
            "organization": "MEF",
            "name": "Joe Order",
            "numberExtension": null
        }
    ]
}

```
## Validation of routing parameters
In some cases, the parameters are used to decide the routes in component files like ‘api.order.yaml', 
and if these parameters are not correct, then the subsequent steps cannot proceed as expected. 
Therefore, the validation on these routing variables has been put ahead in component files and the error message will be reported in the 'JavaScriptEngineActionRunner' class.



## Testing the validation rules

1.Copying the contents of the folder ‘kraken-open/kraken-app/kraken-app-hub/src/test/resources/mock' into the folder 'kraken-open/kraken-app/kraken-app-hub/src/main/resources/mock';

2.Uncommenting the ‘componentPaths' in the file 'kraken-open/kraken-app/kraken-app-hub/src/main/resources/mock/product.yaml’;

3.Clearing all the records of the following 3 tables one by one('kraken_asset_link', ‘kraken_asset_facet', 'kraken_asset’);

4.Starting the Application in the ' 'kraken-open/kraken-app/kraken-app-hub/';

5.Sending requests in the postman to check whether your validation rules take effect.