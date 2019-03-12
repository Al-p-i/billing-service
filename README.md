# billing-service
[![Build Status](https://travis-ci.com/Al-p-i/billing-service.svg?branch=master)](https://travis-ci.com/Al-p-i/billing-service)  
Simple billing service on Kotlin + [Spark-java](http://sparkjava.com/). Provides json API.

## API:
```
Create account:
POST /account/create[?amount=$amount]

Transfer money between accounts:
PUT /transfer?from=$from_id&to=$to_id&amount=$amount

List accounts:
GET /account/list

Account info:
GET /account/id/$id
```
