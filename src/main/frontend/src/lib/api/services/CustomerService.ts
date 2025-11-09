/* generated using openapi-typescript-codegen -- do no edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { paths_1customers_post_requestBody_content_application_1json_schema } from '../models/paths_1customers_post_requestBody_content_application_1json_schema';
import type { paths_1customers_post_responses_201_content_application_1json_schema } from '../models/paths_1customers_post_responses_201_content_application_1json_schema';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class CustomerService {
    /**
     * List customers
     * Retrieve a paginated list of customers.
     * @returns any A page of customers.
     * @throws ApiError
     */
    public static listCustomers({
        page,
        size,
        sort,
    }: {
        /**
         * Page number (0-based).
         */
        page?: number,
        /**
         * Page size (number of elements per page).
         */
        size?: number,
        /**
         * Sorting criteria in the format: property(,asc|desc). Multiple sort params are supported.
         */
        sort?: Array<string>,
    }): CancelablePromise<{
        content?: Array<paths_1customers_post_responses_201_content_application_1json_schema>;
        /**
         * Current page number (0-based).
         */
        number?: number;
        /**
         * The size of the page.
         */
        size?: number;
        /**
         * Total number of elements available.
         */
        totalElements?: number;
        /**
         * Total number of pages available.
         */
        totalPages?: number;
        /**
         * Sorting configuration for the page contents.
         */
        sort?: {
            sorted?: boolean;
            unsorted?: boolean;
            empty?: boolean;
        };
        first?: boolean;
        last?: boolean;
        numberOfElements?: number;
    }> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/customers',
            query: {
                'page': page,
                'size': size,
                'sort': sort,
            },
            errors: {
                400: `Problem`,
            },
        });
    }
    /**
     * Create customer
     * Create a new customer. Returns 201 with the created resource and a Location header.
     * @returns any Created
     * @throws ApiError
     */
    public static createCustomer({
        requestBody,
    }: {
        requestBody: {
            name: string;
            email?: string;
            phoneNumber?: string;
            addressLine1: string;
            addressLine2?: string;
            city: string;
            state: string;
            postalCode: string;
        },
    }): CancelablePromise<{
        id: number;
        version?: number;
        name: string;
        email?: string;
        phoneNumber?: string;
        addressLine1: string;
        addressLine2?: string;
        city: string;
        state: string;
        postalCode: string;
        createdDate?: string;
        updateDate?: string;
    }> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/customers',
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                400: `Problem`,
            },
        });
    }
    /**
     * Get customer by id
     * Retrieve a customer by its identifier.
     * @returns paths_1customers_post_responses_201_content_application_1json_schema Customer found
     * @throws ApiError
     */
    public static getCustomer({
        id,
    }: {
        id: number,
    }): CancelablePromise<paths_1customers_post_responses_201_content_application_1json_schema> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/customers/{id}',
            path: {
                'id': id,
            },
            errors: {
                404: `Problem`,
            },
        });
    }
    /**
     * Update customer
     * Update customer fields by id.
     * @returns paths_1customers_post_responses_201_content_application_1json_schema Updated
     * @throws ApiError
     */
    public static updateCustomer({
        id,
        requestBody,
    }: {
        id: number,
        requestBody: paths_1customers_post_requestBody_content_application_1json_schema,
    }): CancelablePromise<paths_1customers_post_responses_201_content_application_1json_schema> {
        return __request(OpenAPI, {
            method: 'PUT',
            url: '/customers/{id}',
            path: {
                'id': id,
            },
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                400: `Problem`,
                404: `Problem`,
            },
        });
    }
    /**
     * Patch customer
     * Partially update customer fields by id.
     * @returns paths_1customers_post_responses_201_content_application_1json_schema Patched
     * @throws ApiError
     */
    public static patchCustomer({
        id,
        requestBody,
    }: {
        id: number,
        requestBody: paths_1customers_post_requestBody_content_application_1json_schema,
    }): CancelablePromise<paths_1customers_post_responses_201_content_application_1json_schema> {
        return __request(OpenAPI, {
            method: 'PATCH',
            url: '/customers/{id}',
            path: {
                'id': id,
            },
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                400: `Problem`,
                404: `Problem`,
            },
        });
    }
    /**
     * Delete customer
     * Delete customer by id.
     * @returns void
     * @throws ApiError
     */
    public static deleteCustomer({
        id,
    }: {
        id: number,
    }): CancelablePromise<void> {
        return __request(OpenAPI, {
            method: 'DELETE',
            url: '/customers/{id}',
            path: {
                'id': id,
            },
            errors: {
                404: `Problem`,
                409: `Problem`,
            },
        });
    }
}
