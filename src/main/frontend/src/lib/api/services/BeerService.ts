/* generated using openapi-typescript-codegen -- do no edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { paths_1beers_post_responses_201_content_application_1json_schema } from '../models/paths_1beers_post_responses_201_content_application_1json_schema';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class BeerService {
    /**
     * List beers
     * Retrieve beers with pagination. Optionally filter by beanName (matches beerName field, contains, case-insensitive) and beerStyle (contains, case-insensitive).
     * @returns any A page of beers.
     * @throws ApiError
     */
    public static listBeers({
        page,
        size,
        beanName,
        beerStyle,
    }: {
        /**
         * Zero-based page index.
         */
        page: number,
        /**
         * The size of the page to be returned.
         */
        size: number,
        /**
         * Optional filter that matches Beer.beerName (contains, case-insensitive).
         */
        beanName?: string,
        /**
         * Optional filter that matches Beer.beerStyle (contains, case-insensitive).
         */
        beerStyle?: string,
    }): CancelablePromise<{
        content?: Array<paths_1beers_post_responses_201_content_application_1json_schema>;
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
            url: '/beers',
            query: {
                'page': page,
                'size': size,
                'beanName': beanName,
                'beerStyle': beerStyle,
            },
            errors: {
                400: `Problem`,
            },
        });
    }
    /**
     * Create beer
     * Create a new beer. Returns 201 with the created resource and a Location header.
     * @returns any Created
     * @throws ApiError
     */
    public static createBeer({
        requestBody,
    }: {
        requestBody: paths_1beers_post_responses_201_content_application_1json_schema,
    }): CancelablePromise<{
        /**
         * Unique identifier assigned by the server.
         */
        readonly id?: number;
        /**
         * Version number for optimistic locking, assigned by the server.
         */
        readonly version?: number;
        /**
         * Human-friendly name of the beer.
         */
        beerName: string;
        /**
         * Style of the beer (for example: "ALE", "PALE ALE", "IPA", "STOUT").
         *
         */
        beerStyle: string;
        /**
         * Universal Product Code for the beer. Expected to be a 13-digit numeric string.
         *
         */
        upc: string;
        /**
         * Number of units available in inventory. Must be zero or positive.
         */
        quantityOnHand: number;
        /**
         * Unit price of the beer. Must be strictly positive.
         */
        price: number;
        /**
         * Optional textual description of the beer.
         */
        description?: string;
        /**
         * Timestamp when the beer was created (server-managed).
         */
        readonly createdDate?: string;
        /**
         * Timestamp when the beer was last updated (server-managed).
         */
        readonly updateDate?: string;
    }> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/beers',
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                400: `Problem`,
            },
        });
    }
    /**
     * Get beer by ID
     * @returns paths_1beers_post_responses_201_content_application_1json_schema Beer found
     * @throws ApiError
     */
    public static getBeerById({
        id,
    }: {
        /**
         * Numeric identifier of the beer.
         */
        id: number,
    }): CancelablePromise<paths_1beers_post_responses_201_content_application_1json_schema> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/beers/{id}',
            path: {
                'id': id,
            },
            errors: {
                404: `Problem`,
            },
        });
    }
    /**
     * Update beer
     * Update an existing beer by its ID.
     * @returns paths_1beers_post_responses_201_content_application_1json_schema Updated
     * @throws ApiError
     */
    public static updateBeer({
        id,
        requestBody,
    }: {
        /**
         * Numeric identifier of the beer.
         */
        id: number,
        requestBody: paths_1beers_post_responses_201_content_application_1json_schema,
    }): CancelablePromise<paths_1beers_post_responses_201_content_application_1json_schema> {
        return __request(OpenAPI, {
            method: 'PUT',
            url: '/beers/{id}',
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
     * Partially update beer
     * Partially update an existing beer by its ID. Only provided fields will be updated.
     * @returns paths_1beers_post_responses_201_content_application_1json_schema Patched
     * @throws ApiError
     */
    public static patchBeer({
        id,
        requestBody,
    }: {
        /**
         * Numeric identifier of the beer.
         */
        id: number,
        requestBody: {
            /**
             * Human-friendly name of the beer.
             */
            beerName?: string;
            /**
             * Style of the beer (e.g., ALE, IPA, STOUT).
             */
            beerStyle?: string;
            /**
             * 13-digit Universal Product Code.
             */
            upc?: string;
            /**
             * Number of units available in inventory (must be zero or positive).
             */
            quantityOnHand?: number;
            /**
             * Unit price of the beer (must be strictly positive).
             */
            price?: number;
            /**
             * Optional textual description of the beer.
             */
            description?: string;
        },
    }): CancelablePromise<paths_1beers_post_responses_201_content_application_1json_schema> {
        return __request(OpenAPI, {
            method: 'PATCH',
            url: '/beers/{id}',
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
     * Delete beer
     * @returns void
     * @throws ApiError
     */
    public static deleteBeer({
        id,
    }: {
        /**
         * Numeric identifier of the beer.
         */
        id: number,
    }): CancelablePromise<void> {
        return __request(OpenAPI, {
            method: 'DELETE',
            url: '/beers/{id}',
            path: {
                'id': id,
            },
            errors: {
                404: `Problem`,
            },
        });
    }
}
