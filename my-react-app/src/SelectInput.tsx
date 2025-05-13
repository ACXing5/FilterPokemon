import React from 'react';

interface Option {
    id: string;
    displayName: string;
}

interface SelectInputProps {
    id: string;
    onChange: (optionId: Option['id']) => void;
    options: Option[];
}

function SelectInput({ id, onChange, options,}: SelectInputProps) {
    return (
        <select id={id} onChange={(e) => onChange(e.target.value)}>
            {options.map((option) => (
                <option key={option.id} value={option.id}>
                    {option.displayName}
                </option>
            ))}
        </select>
    )
}

function TypeInput({ id, value, onChange }) {
    return (
      <input
        type="text"
        id={id}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder="Type your input here..."
        style={{ width: '200px', height: '40px', fontSize: '16px' }}
      />
    );
  }

export {SelectInput, TypeInput};