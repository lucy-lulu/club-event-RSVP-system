import React, { useState } from "react";
import { API_BASE_URL } from "../../App";
import { Form, Input, Button, notification } from "antd";

const CreateFundingApplication = () => {
    const [loading, setLoading] = useState(false);
    const [form] = Form.useForm();

    const onFinish = async (values) => {
        setLoading(true);
        try {
            const token = localStorage.getItem("token");
            const response = await fetch(`${API_BASE_URL}/create-funding`, {
                method: "POST",
                body: JSON.stringify({
                    club_id: values.club_id,
                    amount: values.amount,
                    description: values.description,
                    semester: values.semester,
                }),
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
            });

            if (!response.ok) {
                const error = await response.json();
                throw new Error(`Error: ${error.error || error.message}`);
            }

            const data = await response.json();

            notification.success({
                message: "Success",
                description:
                    data?.message ||
                    "Funding application created successfully!",
            });
            form.resetFields();
        } catch (error) {
            console.error("Error creating funding application:", error);
            const errorMessage = String(error).split("Error:");
            notification.error({
                message: "Error",
                description:
                    "Failed to create funding application. " +
                    errorMessage[errorMessage.length - 1],
            });
        } finally {
            setLoading(false);
        }
    };

    return (
        <div>
            <h1>Create Funding Application</h1>
            <Form
                form={form}
                layout="vertical"
                onFinish={onFinish}
                initialValues={{
                    club_id: "",
                    amount: "",
                    description: "",
                    semester: "",
                }}
            >
                <Form.Item
                    label="Club ID"
                    name="club_id"
                    rules={[
                        {
                            required: true,
                            message: "Please input the Club ID!",
                        },
                    ]}
                >
                    <Input placeholder="Enter your Club ID" />
                </Form.Item>

                <Form.Item
                    label="Amount"
                    name="amount"
                    rules={[
                        {
                            required: true,
                            message: "Please input the requested amount!",
                        },
                    ]}
                >
                    <Input type="number" placeholder="Enter amount" />
                </Form.Item>

                <Form.Item
                    label="Description"
                    name="description"
                    rules={[
                        {
                            required: true,
                            message: "Please input the description!",
                        },
                    ]}
                >
                    <Input.TextArea placeholder="Describe the reason for funding" />
                </Form.Item>

                <Form.Item
                    label="Semester"
                    name="semester"
                    rules={[
                        {
                            required: true,
                            message:
                                "Please input the semester (e.g., 2023s2)!",
                        },
                    ]}
                >
                    <Input placeholder="Enter semester (e.g., 2023s2)" />
                </Form.Item>

                <Form.Item>
                    <Button type="primary" htmlType="submit" loading={loading}>
                        Submit
                    </Button>
                </Form.Item>
            </Form>
        </div>
    );
};

export default CreateFundingApplication;
