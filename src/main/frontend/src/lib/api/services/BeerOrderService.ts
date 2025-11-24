/* generated using openapi-typescript-codegen -- do no edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { paths_1api_1v1_1beer_orders_1_orderId_1shipments_post_requestBody_content_application_1json_schema } from '../models/paths_1api_1v1_1beer_orders_1_orderId_1shipments_post_requestBody_content_application_1json_schema';
import type { paths_1api_1v1_1beer_orders_1_orderId_1shipments_post_responses_201_content_application_1json_schema } from '../models/paths_1api_1v1_1beer_orders_1_orderId_1shipments_post_responses_201_content_application_1json_schema';
import type { paths_1orders_post_responses_201_content_application_1json_schema } from '../models/paths_1orders_post_responses_201_content_application_1json_schema';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class BeerOrderService {
    /**
     * List orders
     * Retrieve a paginated list of beer orders.
     * @returns any A page of orders.
     * @throws ApiError
     */
    public static listOrders({
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
        content?: Array<paths_1orders_post_responses_201_content_application_1json_schema>;
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
            url: '/orders',
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
     * Create order
     * Create a new beer order. Returns 201 with the created resource and a Location header.
     * @returns any Created
     * @throws ApiError
     */
    public static createOrder({
        requestBody,
    }: {
        requestBody: paths_1orders_post_responses_201_content_application_1json_schema,
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
         * Optional reference provided by the customer for correlation (must not be blank).
         */
        customerRef: string;
        /**
         * List of order lines. Must contain at least one item.
         */
        orderLines: Array<{
            /**
             * Unique identifier of the order line (server-assigned).
             */
            readonly id?: number;
            /**
             * Version for optimistic locking (server-managed).
             */
            readonly version?: number;
            /**
             * Identifier of the beer being ordered.
             */
            beerId: number;
            /**
             * Number of units requested for this beer. Must be a positive integer.
             */
            orderQuantity: number;
            /**
             * Unit price captured at the time of ordering. May be omitted for price lookup on server.
             */
            price?: number;
            /**
             * Timestamp when this order line was created.
             */
            readonly createdDate?: string;
            /**
             * Timestamp when this order line was last updated.
             */
            readonly updateDate?: string;
        }>;
        /**
         * Timestamp when the order was created (server-managed).
         */
        readonly createdDate?: string;
        /**
         * Timestamp when the order was last updated (server-managed).
         */
        readonly updateDate?: string;
    }> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/orders',
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                400: `Problem`,
            },
        });
    }
    /**
     * Get order by ID
     * Retrieve a single beer order by its identifier.
     * @returns paths_1orders_post_responses_201_content_application_1json_schema Beer order found.
     * @throws ApiError
     */
    public static getOrder({
        id,
    }: {
        /**
         * Unique identifier of the beer order.
         */
        id: number,
    }): CancelablePromise<paths_1orders_post_responses_201_content_application_1json_schema> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/orders/{id}',
            path: {
                'id': id,
            },
            errors: {
                400: `Problem`,
                404: `Problem`,
            },
        });
    }
    /**
     * Update order
     * Replace a beer order by ID.
     * @returns paths_1orders_post_responses_201_content_application_1json_schema Updated
     * @throws ApiError
     */
    public static updateOrder({
        id,
        requestBody,
    }: {
        /**
         * Unique identifier of the beer order.
         */
        id: number,
        requestBody: paths_1orders_post_responses_201_content_application_1json_schema,
    }): CancelablePromise<paths_1orders_post_responses_201_content_application_1json_schema> {
        return __request(OpenAPI, {
            method: 'PUT',
            url: '/orders/{id}',
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
     * Delete order
     * Delete a beer order by ID. Idempotent operation.
     * @returns void
     * @throws ApiError
     */
    public static deleteOrder({
        id,
    }: {
        /**
         * Unique identifier of the beer order.
         */
        id: number,
    }): CancelablePromise<void> {
        return __request(OpenAPI, {
            method: 'DELETE',
            url: '/orders/{id}',
            path: {
                'id': id,
            },
            errors: {
                400: `Problem`,
                404: `Problem`,
            },
        });
    }
    /**
     * List shipments for a beer order
     * @returns any Page of shipments
     * @throws ApiError
     */
    public static listBeerOrderShipments({
        orderId,
        page,
        size = 20,
        sort,
    }: {
        orderId: number,
        page?: number,
        size?: number,
        sort?: string,
    }): CancelablePromise<{
        content?: Array<paths_1api_1v1_1beer_orders_1_orderId_1shipments_post_responses_201_content_application_1json_schema>;
        pageable?: Record<string, any>;
        totalElements?: number;
        totalPages?: number;
        number?: number;
    }> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/v1/beer-orders/{orderId}/shipments',
            path: {
                'orderId': orderId,
            },
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
     * Create a shipment for a beer order
     * @returns any Created
     * @throws ApiError
     */
    public static createBeerOrderShipment({
        orderId,
        requestBody,
    }: {
        orderId: number,
        requestBody: {
            /**
             * ID of the BeerOrder this shipment belongs to.
             */
            beerOrderId: number;
            shipmentDate: string;
            carrier: string;
            carrierNumber: string;
        },
    }): CancelablePromise<{
        id: number;
        beerOrderId: number;
        shipmentDate: string;
        carrier: string;
        carrierNumber: string;
        createdDate?: string;
        updateDate?: string;
    }> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/v1/beer-orders/{orderId}/shipments',
            path: {
                'orderId': orderId,
            },
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                400: `Problem`,
            },
        });
    }
    /**
     * Get shipment by id
     * @returns paths_1api_1v1_1beer_orders_1_orderId_1shipments_post_responses_201_content_application_1json_schema Shipment found
     * @throws ApiError
     */
    public static getBeerOrderShipment({
        orderId,
        id,
    }: {
        orderId: number,
        id: number,
    }): CancelablePromise<paths_1api_1v1_1beer_orders_1_orderId_1shipments_post_responses_201_content_application_1json_schema> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/v1/beer-orders/{orderId}/shipments/{id}',
            path: {
                'orderId': orderId,
                'id': id,
            },
            errors: {
                404: `Problem`,
            },
        });
    }
    /**
     * Update shipment
     * @returns paths_1api_1v1_1beer_orders_1_orderId_1shipments_post_responses_201_content_application_1json_schema Updated
     * @throws ApiError
     */
    public static updateBeerOrderShipment({
        orderId,
        id,
        requestBody,
    }: {
        orderId: number,
        id: number,
        requestBody: paths_1api_1v1_1beer_orders_1_orderId_1shipments_post_requestBody_content_application_1json_schema,
    }): CancelablePromise<paths_1api_1v1_1beer_orders_1_orderId_1shipments_post_responses_201_content_application_1json_schema> {
        return __request(OpenAPI, {
            method: 'PUT',
            url: '/api/v1/beer-orders/{orderId}/shipments/{id}',
            path: {
                'orderId': orderId,
                'id': id,
            },
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                404: `Problem`,
            },
        });
    }
    /**
     * Delete shipment
     * @returns void
     * @throws ApiError
     */
    public static deleteBeerOrderShipment({
        orderId,
        id,
    }: {
        orderId: number,
        id: number,
    }): CancelablePromise<void> {
        return __request(OpenAPI, {
            method: 'DELETE',
            url: '/api/v1/beer-orders/{orderId}/shipments/{id}',
            path: {
                'orderId': orderId,
                'id': id,
            },
            errors: {
                404: `Problem`,
            },
        });
    }
}
